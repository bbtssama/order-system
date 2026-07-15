package com.atguigu.order.mapper;

import com.atguigu.order.bean.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartMapper {

    /** 新增购物车项（回填自增ID） */
    int insert(CartItem cartItem);

    /** 查询用户购物车列表（LEFT JOIN product 获取商品信息） */
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    /** 查询用户购物车中某商品（判断是否已加入，避免重复添加） */
    CartItem selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /** 根据ID和用户ID查询购物车项（同时校验所有权） */
    CartItem selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /** 修改购物车项数量 */
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /** 根据ID删除购物车项 */
    int deleteById(@Param("id") Long id);

    /** 清空用户购物车 */
    int deleteByUserId(@Param("userId") Long userId);
}
