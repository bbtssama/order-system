package com.atguigu.order.mapper;

import com.atguigu.order.bean.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper {

    /** 新增商品 */
    int insert(Product product);

    /** 更新商品 */
    int update(Product product);

    /** 根据ID删除商品 */
    int deleteById(@Param("id") Long id);

    /** 根据ID查询商品 */
    Product selectById(@Param("id") Long id);

    /** 分页查询商品（支持按名称模糊搜索和状态筛选） */
    List<Product> selectPage(@Param("offset") Long offset, @Param("limit") Integer limit,
                             @Param("name") String name, @Param("status") String status);

    /** 查询商品总数（支持按名称模糊搜索和状态筛选） */
    Long selectCount(@Param("name") String name, @Param("status") String status);

    /** 按分类分页查询上架商品 */
    List<Product> selectByCategory(@Param("categoryId") Long categoryId,
                                   @Param("offset") Long offset,
                                   @Param("limit") Integer limit);

    /** 按分类查询上架商品总数 */
    Long selectCountByCategory(@Param("categoryId") Long categoryId);

    /**
     * 扣减库存（CAS 乐观策略）
     * <p>
     * 通过 WHERE stock >= quantity 保证库存不会被扣成负数，防止超卖。
     * 返回受影响行数：1 表示扣减成功，0 表示库存不足需重试。
     * </p>
     */
    int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 恢复库存（用于取消订单时回退库存）
     */
    int incrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
