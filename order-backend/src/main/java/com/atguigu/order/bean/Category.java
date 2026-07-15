package com.atguigu.order.bean;

import lombok.Data;

/**
 * 分类实体类
 * <p>
 * 对应数据库 category 表，用于商品归类。
 * </p>
 */
@Data
public class Category {
    /** 主键ID */
    private Long id;
    /** 分类名称 */
    private String name;
}
