package com.atguigu.order.service.impl;

import com.atguigu.order.bean.CartItem;
import com.atguigu.order.bean.Order;
import com.atguigu.order.bean.OrderItem;
import com.atguigu.order.bean.Product;
import com.atguigu.order.vo.OrderItemRequest;
import com.atguigu.order.common.BusinessException;
import com.atguigu.order.common.PageResult;
import com.atguigu.order.common.R;
import com.atguigu.order.mapper.CartMapper;
import com.atguigu.order.mapper.OrderItemMapper;
import com.atguigu.order.mapper.OrderMapper;
import com.atguigu.order.mapper.ProductMapper;
import com.atguigu.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现
 * <p>
 * 核心职责：
 * <ul>
 *   <li>创建订单：校验库存 → 扣减库存 → 生成订单 → 批量插入订单项 → 清空购物车 → 发送MQ通知</li>
 *   <li>查询订单：支持分页查询和详情查询，自动加载订单项</li>
 *   <li>取消订单：仅允许取消待支付订单，取消后恢复商品库存</li>
 *   <li>更新状态：管理员操作，推进订单流转</li>
 * </ul>
 * 创建订单和取消订单均使用 {@link Transactional} 保证数据一致性。
 * </p>
 */
@Primary
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建订单（事务性操作）
     * <p>
     * 流程：
     * 1. 获取用户购物车中选中的商品（若未指定则下单全部）
     * 2. 逐项校验商品状态、库存和价格一致性（防下单瞬间改价），使用 CAS 方式扣减库存（防止超卖）
     * 3. 生成唯一订单号，插入订单主表
     * 4. 批量插入订单项
     * 5. 从购物车中移除已下单的商品
     * 6. 通过 RabbitMQ 异步发送订单通知消息
     * </p>
     *
     * @param userId  当前登录用户ID
     * @param items   下单明细，携带购物车项ID及用户确认的单价；为空则下单全部
     * @return 包含订单信息和订单项的响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Order> createOrder(Long userId, List<OrderItemRequest> items) {
        // 1. 获取用户购物车
        List<CartItem> allCartItems = cartMapper.selectByUserId(userId);
        if (allCartItems == null || allCartItems.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }

        // 将购物车项按 ID 索引，便于按 cartItemId 快速查找
        Map<Long, CartItem> cartItemMap = new HashMap<>();
        for (CartItem item : allCartItems) {
            cartItemMap.put(item.getId(), item);
        }

        // 筛选要下单的商品：
        // - 若 items 非空，则按 cartItemId 筛选，并保留用户确认的单价用于后续比对
        // - 否则下单购物车全部商品（不进行价格校验，保持向后兼容）
        List<CartItem> cartItemsToOrder;
        Map<Long, OrderItemRequest> requestMap = new HashMap<>();
        List<OrderItemRequest> itemRequests = items;
        if (itemRequests != null && !itemRequests.isEmpty()) {
            cartItemsToOrder = new ArrayList<>();
            for (OrderItemRequest req : itemRequests) {
                CartItem ci = cartItemMap.get(req.getCartItemId());
                if (ci == null) {
                    throw new BusinessException("未选择有效的购物车商品");
                }
                cartItemsToOrder.add(ci);
                requestMap.put(req.getCartItemId(), req);
            }
        } else {
            cartItemsToOrder = allCartItems;
        }

        // 2. 校验库存并扣减，同时构建订单项列表
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItemsToOrder) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException("商品【" + cartItem.getProductName() + "】不存在");
            }
            if ("OFF".equals(product.getStatus())) {
                throw new BusinessException("商品【" + product.getName() + "】已下架");
            }
            if (cartItem.getQuantity() > product.getStock()) {
                throw new BusinessException("商品【" + product.getName() + "】库存不足，当前库存: "
                        + product.getStock() + "，需要: " + cartItem.getQuantity());
            }

            // 价格一致性校验：将用户下单时确认的单价与数据库最新价比对，
            // 若不一致说明下单期间商品已改价，应中止下单并提示用户刷新。
            OrderItemRequest reqItem = requestMap.get(cartItem.getId());
            if (reqItem != null && reqItem.getPrice() != null
                    && product.getPrice().compareTo(reqItem.getPrice()) != 0) {
                throw new BusinessException("商品【" + product.getName() + "】价格发生更改，请刷新后重试");
            }

            // CAS 扣减库存：SQL 的 WHERE 同时校验 status='ON' 和 stock>=quantity，做最终裁决
            // 返回 0 说明并发场景下商品被下架或库存被抢占
            int affected = productMapper.decrementStock(product.getId(), cartItem.getQuantity());
            if (affected == 0) {
                throw new BusinessException("商品【" + product.getName() + "】下单失败（已下架或库存被抢占），请刷新后重试");
            }

            // 计算订单项金额
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // 构建订单项（快照商品名称和价格，防止商品后续修改影响历史订单）
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        // 3. 生成唯一订单号
        String orderNo = generateOrderNo();

        // 4. 创建订单主记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        orderMapper.insert(order);

        // 5. 批量插入订单项（此时 order.getId() 已通过 useGeneratedKeys 回填）
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.batchInsert(orderItems);

        // 6. 从购物车中移除已下单的商品
        for (CartItem item : cartItemsToOrder) {
            cartMapper.deleteById(item.getId());
        }

        // 7. 异步发送 RabbitMQ 订单通知消息（消息发送失败不影响订单创建）
        try {
            String message = String.format(
                    "{\"orderId\":%d,\"orderNo\":\"%s\",\"userId\":%d,\"totalAmount\":%s}",
                    order.getId(), orderNo, userId, totalAmount);
            rabbitTemplate.convertAndSend("order.exchange", "order.notify", message);
            log.info("订单通知消息已发送: {}", message);
        } catch (Exception e) {
            // 消息发送失败仅记录日志，不回滚事务（可由补偿机制重发）
            log.error("发送订单通知消息失败，订单号: {}", orderNo, e);
        }

        order.setOrderItems(orderItems);
        return R.ok("下单成功", order);
    }

    /**
     * 查询订单详情（含订单项）
     * 仅允许查询属于自己的订单，防止越权访问。
     */
    @Override
    public R<Order> getOrderById(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        order.setOrderItems(items);
        return R.ok(order);
    }

    /**
     * 分页查询当前用户的订单列表
     * 注意：此处存在 N+1 查询（每个订单单独查询订单项），数据量大时应优化为 JOIN 查询。
     */
    @Override
    public R<PageResult<Order>> getOrderPage(Long userId, Integer page, Integer pageSize) {
        long offset = (long) (page - 1) * pageSize;
        List<Order> records = orderMapper.selectPage(userId, offset, pageSize);
        Long total = orderMapper.selectCount(userId);
        // 加载每个订单的订单项
        for (Order order : records) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            order.setOrderItems(items);
        }
        return R.ok(PageResult.of(records, total, page, pageSize));
    }

    /**
     * 取消订单（事务性操作）
     * <p>
     * 仅允许取消状态为 PENDING（待支付）的订单。
     * 取消后通过 incrementStock 恢复商品库存，并更新订单状态为 CANCELLED。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只能取消待支付订单，当前状态: " + order.getStatus());
        }

        // 恢复商品库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        if (items != null) {
            for (OrderItem item : items) {
                productMapper.incrementStock(item.getProductId(), item.getQuantity());
            }
        }

        orderMapper.updateStatus(orderId, "CANCELLED");
        return R.ok("订单已取消");
    }

    /**
     * 更新订单状态（管理员操作）
     * 支持的流转：PENDING → PAID → SHIPPED → COMPLETED
     */
    @Override
    public R<Order> updateOrderStatus(Long orderId, String status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        orderMapper.updateStatus(orderId, status);
        order.setStatus(status);
        return R.ok("订单状态已更新", order);
    }

    /**
     * 生成订单号：ORD + yyyyMMddHHmmss + 6位随机数
     * 使用 ThreadLocalRandom 替代 Math.random()，并发性能更好且线程安全。
     */
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        return "ORD" + datePart + randomPart;
    }
}
