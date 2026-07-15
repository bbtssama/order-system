package com.atguigu.order.controller;

import com.atguigu.order.bean.CartItem;
import com.atguigu.order.common.R;
import com.atguigu.order.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 * <p>
 * 提供购物车的增删改查接口，所有操作均绑定当前登录用户，
 * 通过 JWT 中的 userId 确保用户只能操作自己的购物车。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin
public class CartRestController {

    @Autowired
    private CartService cartService;

    /**
     * 从请求属性中提取当前登录用户ID（由 LoginInterceptor 注入）
     */
    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /** 查看购物车列表（含商品关联信息） */
    @GetMapping
    public R<List<CartItem>> getCartList(HttpServletRequest request) {
        return cartService.getCartList(getUserId(request));
    }

    /** 添加商品到购物车 */
    @PostMapping
    public R<CartItem> addToCart(@RequestBody CartItem cartItem, HttpServletRequest request) {
        return cartService.addToCart(getUserId(request), cartItem);
    }

    /** 修改购物车商品数量 */
    @PutMapping("/{cartItemId}")
    public R<CartItem> updateQuantity(@PathVariable Long cartItemId, @RequestBody CartItem cartItem, HttpServletRequest request) {
        return cartService.updateQuantity(getUserId(request), cartItemId, cartItem.getQuantity());
    }

    /** 删除购物车项 */
    @DeleteMapping("/{cartItemId}")
    public R<Void> deleteCartItem(@PathVariable Long cartItemId, HttpServletRequest request) {
        return cartService.deleteCartItem(getUserId(request), cartItemId);
    }

    /** 清空购物车 */
    @DeleteMapping
    public R<Void> clearCart(HttpServletRequest request) {
        return cartService.clearCart(getUserId(request));
    }
}
