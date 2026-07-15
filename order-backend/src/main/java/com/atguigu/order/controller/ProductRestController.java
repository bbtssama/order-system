package com.atguigu.order.controller;

import com.atguigu.order.bean.Product;
import com.atguigu.order.common.PageResult;
import com.atguigu.order.common.R;
import com.atguigu.order.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 * <p>
 * 提供商品的分页查询、分类筛选、详情查询和 CRUD 接口。
 * 查询接口所有用户可访问，增删改接口实际应由管理员使用（前端通过路由控制权限）。
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class ProductRestController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表（支持按名称搜索和状态筛选）
     */
    @GetMapping("/products")
    public R<PageResult<Product>> getProductPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        return productService.getProductPage(page, pageSize, name, status);
    }

    /**
     * 按分类分页查询上架商品
     */
    @GetMapping("/products/category/{categoryId}")
    public R<PageResult<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return productService.getProductsByCategory(categoryId, page, pageSize);
    }

    /**
     * 查询商品详情（命中 Redis 缓存）
     */
    @GetMapping("/product/{id}")
    public R<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    /** 新增商品 */
    @PostMapping("/product")
    public R<Product> createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    /** 更新商品 */
    @PutMapping("/product")
    public R<Product> updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }

    /** 删除商品 */
    @DeleteMapping("/product/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}
