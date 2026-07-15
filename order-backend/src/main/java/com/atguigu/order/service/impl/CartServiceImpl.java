package com.atguigu.order.service.impl;

import com.atguigu.order.bean.CartItem;
import com.atguigu.order.bean.Product;
import com.atguigu.order.common.BusinessException;
import com.atguigu.order.common.R;
import com.atguigu.order.mapper.CartMapper;
import com.atguigu.order.mapper.ProductMapper;
import com.atguigu.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现
 * <p>
 * 购物车项操作均校验用户所有权，防止越权操作他人购物车。
 * 添加商品时自动合并相同商品的数量，并校验库存上限。
 * </p>
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 查询用户购物车列表（含商品关联信息）
     */
    @Override
    public R<List<CartItem>> getCartList(Long userId) {
        List<CartItem> list = cartMapper.selectByUserId(userId);
        return R.ok(list);
    }

    /**
     * 添加商品到购物车
     * <p>
     * 若购物车中已有该商品，则累加数量并校验库存；否则新增购物车项。
     * </p>
     */
    @Override
    public R<CartItem> addToCart(Long userId, CartItem cartItem) {
        Product product = productMapper.selectById(cartItem.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if ("OFF".equals(product.getStatus())) {
            throw new BusinessException("商品已下架");
        }

        // 数量校验：未传或非正数时默认为 1
        Integer quantity = cartItem.getQuantity();
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        // 库存校验
        if (quantity > product.getStock()) {
            throw new BusinessException("商品库存不足，当前库存: " + product.getStock());
        }

        // 检查购物车中是否已有该商品，有则合并数量
        CartItem exist = cartMapper.selectByUserIdAndProductId(userId, cartItem.getProductId());
        if (exist != null) {
            int newQuantity = exist.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new BusinessException("商品库存不足，当前库存: " + product.getStock()
                        + "，购物车已有: " + exist.getQuantity());
            }
            cartMapper.updateQuantity(exist.getId(), newQuantity);
            exist.setQuantity(newQuantity);
            return R.ok("已更新购物车数量", exist);
        }

        // 新增购物车项
        cartItem.setUserId(userId);
        cartItem.setQuantity(quantity);
        cartMapper.insert(cartItem);
        return R.ok("已添加到购物车", cartItem);
    }

    /**
     * 修改购物车商品数量
     */
    @Override
    public R<CartItem> updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }
        CartItem cartItem = findCartItemById(userId, cartItemId);
        Product product = productMapper.selectById(cartItem.getProductId());
        if (product != null && quantity > product.getStock()) {
            throw new BusinessException("库存不足，当前库存: " + product.getStock());
        }
        cartMapper.updateQuantity(cartItemId, quantity);
        cartItem.setQuantity(quantity);
        return R.ok("已更新数量", cartItem);
    }

    /**
     * 删除购物车项（校验所有权）
     */
    @Override
    public R<Void> deleteCartItem(Long userId, Long cartItemId) {
        findCartItemById(userId, cartItemId);
        cartMapper.deleteById(cartItemId);
        return R.ok("已移除");
    }

    /**
     * 清空购物车
     */
    @Override
    public R<Void> clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
        return R.ok("购物车已清空");
    }

    /**
     * 根据ID查找购物车项并校验所有权
     * 通过 selectByIdAndUserId 一次性完成查询和权限校验，避免遍历整个购物车。
     *
     * @throws BusinessException 购物车项不存在或不属于当前用户时抛出
     */
    private CartItem findCartItemById(Long userId, Long cartItemId) {
        CartItem item = cartMapper.selectByIdAndUserId(cartItemId, userId);
        if (item == null) {
            throw new BusinessException("购物车项不存在");
        }
        return item;
    }
}
