package com.atguigu.order.bean;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * <p>
 * 对应数据库 product 表。status 字段控制上下架：ON（上架）、OFF（下架）。
 * price 使用 BigDecimal 避免浮点精度问题。
 * </p>
 */
@Data
public class Product {
    /** 主键ID */
    private Long id;
    /** 商品名称 */
    private String name;
    /** 商品描述 */
    private String description;
    /** 商品价格（BigDecimal 保证精度） */
    private BigDecimal price;
    /** 库存数量 */
    private Integer stock;
    /** 所属分类ID */
    private Long categoryId;
    /** 商品图片URL */
    private String imageUrl;
    /** 状态：ON 上架 / OFF 下架 */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;
}
