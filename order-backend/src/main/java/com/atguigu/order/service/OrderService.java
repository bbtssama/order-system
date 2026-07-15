package com.atguigu.order.service;

import com.atguigu.order.bean.Order;
import com.atguigu.order.common.PageResult;
import com.atguigu.order.common.R;
import com.atguigu.order.vo.OrderItemRequest;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单（从购物车下单）
     *
     * @param userId  当前用户ID
     * @param items   下单明细，携带购物车项ID及用户确认的单价；为空则下单全部
     */
    R<Order> createOrder(Long userId, List<OrderItemRequest> items);

    /**
     * 查询订单详情（含订单项）
     */
    R<Order> getOrderById(Long userId, Long orderId);

    /**
     * 分页查询当前用户的订单列表
     */
    R<PageResult<Order>> getOrderPage(Long userId, Integer page, Integer pageSize);

    /**
     * 取消订单（仅限待支付状态，取消后恢复库存）
     */
    R<Void> cancelOrder(Long userId, Long orderId);

    /**
     * 更新订单状态（管理员操作）
     */
    R<Order> updateOrderStatus(Long orderId, String status);
}
