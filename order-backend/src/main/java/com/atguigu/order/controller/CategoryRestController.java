package com.atguigu.order.controller;

import com.atguigu.order.bean.Category;
import com.atguigu.order.common.R;
import com.atguigu.order.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 * <p>
 * 提供商品分类的查询和 CRUD 接口。
 * 查询接口所有用户可访问，增删改接口由管理员使用。
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class CategoryRestController {

    @Autowired
    private CategoryService categoryService;

    /** 获取所有分类列表 */
    @GetMapping("/categories")
    public R<List<Category>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    /** 新增分类 */
    @PostMapping("/category")
    public R<Category> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    /** 更新分类 */
    @PutMapping("/category")
    public R<Category> updateCategory(@RequestBody Category category) {
        return categoryService.updateCategory(category);
    }

    /** 删除分类 */
    @DeleteMapping("/category/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
