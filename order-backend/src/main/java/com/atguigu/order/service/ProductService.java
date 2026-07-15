package com.atguigu.order.service;

import com.atguigu.order.bean.Product;
import com.atguigu.order.common.PageResult;
import com.atguigu.order.common.R;

/**
 * 商品服务接口
 */
public interface ProductService {

    /** 根据ID查询商品详情（带 Redis 缓存） */
    R<Product> getProductById(Long id);

    /** 分页查询商品列表 */
    R<PageResult<Product>> getProductPage(Integer page, Integer pageSize, String name, String status);

    /** 按分类分页查询上架商品 */
    R<PageResult<Product>> getProductsByCategory(Long categoryId, Integer page, Integer pageSize);

    /** 新增商品 */
    R<Product> createProduct(Product product);

    /** 更新商品（清除缓存） */
    R<Product> updateProduct(Product product);

    /** 删除商品（清除缓存） */
    R<Void> deleteProduct(Long id);
}
