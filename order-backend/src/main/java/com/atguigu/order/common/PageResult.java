package com.atguigu.order.common;

import lombok.Data;
import java.util.List;

/**
 * 分页查询结果封装
 * <p>
 * 用于统一封装分页查询的返回数据，包含当前页记录列表和分页元信息。
 * 通过 {@link #of(List, long, long, long)} 静态工厂方法快速构建。
 * </p>
 *
 * @param <T> 记录的数据类型
 */
@Data
public class PageResult<T> {
    /** 当前页记录列表 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private long page;
    /** 每页条数 */
    private long pageSize;

    /**
     * 静态工厂方法，快速构建分页结果
     *
     * @param records  当前页记录列表
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页条数
     */
    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        return result;
    }
}
