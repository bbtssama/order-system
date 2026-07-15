package com.atguigu.order.bean;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 购物车项实体类
 * <p>
 * 对应数据库 cart_item 表，记录用户购物车中的商品及数量。
 * 关联商品字段（productName、productPrice 等）非数据库列，
 * 由 CartMapper.xml 中的 LEFT JOIN 查询填充。
 * </p>
 */
@Data
public class CartItem {
    /** 主键ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 商品ID */
    private Long productId;
    /** 购买数量 */
    private Integer quantity;
    /** 创建时间 */
    private LocalDateTime createTime;

    // ===== 关联商品信息（非数据库字段，由 JOIN 查询填充） =====
    /** 商品名称 */
    private String productName;
    /** 商品单价 */
    private java.math.BigDecimal productPrice;
    /** 商品图片URL */
    private String productImageUrl;
    /** 商品当前库存 */
    private Integer productStock;
}
