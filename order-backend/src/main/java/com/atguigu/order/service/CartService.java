package com.atguigu.order.service;

import com.atguigu.order.bean.CartItem;
import com.atguigu.order.common.R;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /** 查询用户购物车列表 */
    R<List<CartItem>> getCartList(Long userId);

    /** 添加商品到购物车 */
    R<CartItem> addToCart(Long userId, CartItem cartItem);

    /** 修改购物车商品数量 */
    R<CartItem> updateQuantity(Long userId, Long cartItemId, Integer quantity);

    /** 删除购物车项 */
    R<Void> deleteCartItem(Long userId, Long cartItemId);

    /** 清空购物车 */
    R<Void> clearCart(Long userId);
}
