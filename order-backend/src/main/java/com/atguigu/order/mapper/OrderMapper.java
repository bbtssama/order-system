package com.atguigu.order.mapper;

import com.atguigu.order.bean.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    /** 新增订单（回填自增ID） */
    int insert(Order order);

    /** 根据ID查询订单 */
    Order selectById(@Param("id") Long id);

    /** 根据订单号查询订单 */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /** 查询用户的所有订单 */
    List<Order> selectByUserId(@Param("userId") Long userId);

    /** 分页查询用户订单 */
    List<Order> selectPage(@Param("userId") Long userId, @Param("offset") Long offset,
                           @Param("limit") Integer limit);

    /** 查询用户订单总数 */
    Long selectCount(@Param("userId") Long userId);

    /** 更新订单状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
