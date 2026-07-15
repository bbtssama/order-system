package com.atguigu.order.service.impl;

import com.atguigu.order.bean.Category;
import com.atguigu.order.common.BusinessException;
import com.atguigu.order.common.R;
import com.atguigu.order.mapper.CategoryMapper;
import com.atguigu.order.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 * <p>
 * 提供商品分类的 CRUD 操作。分类用于商品归类和前端筛选。
 * </p>
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /** 查询所有分类（按ID升序） */
    @Override
    public R<List<Category>> getAllCategories() {
        List<Category> list = categoryMapper.selectAll();
        return R.ok(list);
    }

    /** 根据ID查询分类 */
    @Override
    public R<Category> getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return R.ok(category);
    }

    /** 新增分类 */
    @Override
    public R<Category> createCategory(Category category) {
        categoryMapper.insert(category);
        return R.ok("新增分类成功", category);
    }

    /** 更新分类（先校验存在性） */
    @Override
    public R<Category> updateCategory(Category category) {
        Category exist = categoryMapper.selectById(category.getId());
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }
        categoryMapper.update(category);
        return R.ok("更新分类成功", category);
    }

    /** 删除分类（先校验存在性） */
    @Override
    public R<Void> deleteCategory(Long id) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }
        categoryMapper.deleteById(id);
        return R.ok("删除分类成功");
    }
}
