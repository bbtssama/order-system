package com.atguigu.order.mapper;

import com.atguigu.order.bean.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项 Mapper 接口
 */
@Mapper
public interface OrderItemMapper {

    /** 新增单个订单项 */
    int insert(OrderItem orderItem);

    /** 批量插入订单项（下单时使用） */
    int batchInsert(@Param("list") List<OrderItem> list);

    /** 根据订单ID查询订单项列表 */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
