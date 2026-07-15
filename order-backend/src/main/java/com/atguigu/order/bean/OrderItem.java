package com.atguigu.order.bean;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单项实体类
 * <p>
 * 对应数据库 order_item 表，记录订单中每个商品的快照信息。
 * 商品名称和价格在订单创建时快照保存，不受后续商品修改影响。
 * </p>
 */
@Data
public class OrderItem {
    /** 主键ID */
    private Long id;
    /** 所属订单ID */
    private Long orderId;
    /** 商品ID */
    private Long productId;
    /** 商品名称（下单时的快照） */
    private String productName;
    /** 商品单价（下单时的快照） */
    private BigDecimal productPrice;
    /** 购买数量 */
    private Integer quantity;
    /** 小计金额（单价 × 数量） */
    private BigDecimal totalPrice;
}
