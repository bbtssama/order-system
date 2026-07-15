package com.atguigu.order.bean;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * <p>
 * 对应数据库 order 表，记录订单主信息。
 * status 字段表示订单状态：PENDING（待支付）、PAID（已支付）、SHIPPED（已发货）、
 * COMPLETED（已完成）、CANCELLED（已取消）。
 * orderItems 为关联订单项列表，非数据库字段，由 Service 层填充。
 * </p>
 */
@Data
public class Order {
    /** 主键ID */
    private Long id;
    /** 订单号（唯一，格式：ORD + yyyyMMddHHmmss + 6位随机数） */
    private String orderNo;
    /** 下单用户ID */
    private Long userId;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 订单状态：PENDING / PAID / SHIPPED / COMPLETED / CANCELLED */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 关联订单项列表（非数据库字段，由 Service 层填充） */
    private java.util.List<OrderItem> orderItems;
}
