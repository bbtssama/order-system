package com.atguigu.order.controller;

import com.atguigu.order.bean.Order;
import com.atguigu.order.common.BusinessException;
import com.atguigu.order.common.PageResult;
import com.atguigu.order.common.R;
import com.atguigu.order.service.OrderService;
import com.atguigu.order.vo.OrderItemRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * <p>
 * 提供订单的创建、查询、取消和状态更新接口。
 * 用户相关操作（创建、查询、取消）自动绑定当前登录用户ID，防止越权。
 * 状态更新为管理员操作，需校验 ADMIN 角色。
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    /**
     * 从请求属性中提取当前登录用户ID（由 LoginInterceptor 注入）
     */
    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /**
     * 从请求属性中提取当前用户角色
     */
    private String getRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    /**
     * 创建订单（从购物车下单）
     * 请求体携带下单明细（含用户确认的单价），为空则下单购物车全部商品。
     */
    @PostMapping("/order")
    public R<Order> createOrder(HttpServletRequest request, @RequestBody(required = false) List<OrderItemRequest> items) {
        return orderService.createOrder(getUserId(request), items);
    }

    /**
     * 查看订单详情（仅允许查看自己的订单）
     */
    @GetMapping("/order/{orderId}")
    public R<Order> getOrderById(@PathVariable Long orderId, HttpServletRequest request) {
        return orderService.getOrderById(getUserId(request), orderId);
    }

    /**
     * 分页查询当前用户的订单列表
     */
    @GetMapping("/orders")
    public R<PageResult<Order>> getOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        return orderService.getOrderPage(getUserId(request), page, pageSize);
    }

    /**
     * 取消订单（仅限待支付状态，取消后恢复库存）
     */
    @PutMapping("/order/{orderId}/cancel")
    public R<Void> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        return orderService.cancelOrder(getUserId(request), orderId);
    }

    /**
     * 更新订单状态（管理员操作）
     * 仅 ADMIN 角色可调用，用于推进订单状态流转。
     */
    @PutMapping("/order/{orderId}/status")
    public R<Order> updateOrderStatus(@PathVariable Long orderId, @RequestBody Order order, HttpServletRequest request) {
        if (!"ADMIN".equals(getRole(request))) {
            throw new BusinessException(403, "无权限操作，仅管理员可更新订单状态");
        }
        return orderService.updateOrderStatus(orderId, order.getStatus());
    }
}
