package com.atguigu.order.mapper;

import com.atguigu.order.bean.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类 Mapper 接口
 */
@Mapper
public interface CategoryMapper {

    /** 新增分类（回填自增ID） */
    int insert(Category category);

    /** 更新分类 */
    int update(Category category);

    /** 根据ID删除分类 */
    int deleteById(@Param("id") Long id);

    /** 根据ID查询分类 */
    Category selectById(@Param("id") Long id);

    /** 查询所有分类（按ID升序） */
    List<Category> selectAll();
}
