package com.atguigu.order.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 下单项请求 VO
 * <p>
 * 携带用户下单时确认的购物车项明细（含单价），供后端与数据库最新价比对，
 * 防止下单瞬间商品价格已变更而导致用户预期金额与实际成交金额不一致。
 * </p>
 */
@Data
public class OrderItemRequest {

    /** 购物车项ID */
    private Long cartItemId;

    /** 用户下单时确认的商品单价（与服务端最新价比对，防价格漂移） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;
}
