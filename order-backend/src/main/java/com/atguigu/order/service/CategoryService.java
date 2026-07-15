package com.atguigu.order.service;

import com.atguigu.order.bean.Category;
import com.atguigu.order.common.R;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /** 获取所有分类 */
    R<List<Category>> getAllCategories();

    /** 根据ID查询分类 */
    R<Category> getCategoryById(Long id);

    /** 新增分类 */
    R<Category> createCategory(Category category);

    /** 更新分类 */
    R<Category> updateCategory(Category category);

    /** 删除分类 */
    R<Void> deleteCategory(Long id);
}
