package com.atguigu.order.common;

import lombok.Data;

/**
 * 统一响应结果封装
 * <p>
 * 所有 API 接口统一返回此结构，包含状态码、消息和数据：
 * <pre>
 * { "code": 200, "msg": "操作成功", "data": {...} }
 * </pre>
 * 提供静态工厂方法 ok() 和 fail() 简化构建。
 * </p>
 *
 * @param <T> data 字段的数据类型
 */
@Data
public class R<T> {
    /** 状态码：200 成功，其他为失败 */
    private Integer code;
    /** 提示消息 */
    private String msg;
    /** 业务数据 */
    private T data;

    /** 成功响应（无数据） */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = null;
        return r;
    }

    /** 成功响应（仅自定义消息，无数据，返回 R<Void>） */
    public static R<Void> ok(String msg) {
        R<Void> r = new R<>();
        r.code = 200;
        r.msg = msg;
        r.data = null;
        return r;
    }

    /** 成功响应（带数据） */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    /** 成功响应（自定义消息和数据） */
    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = msg;
        r.data = data;
        return r;
    }

    /** 失败响应（默认 500 错误码） */
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.code = 500;
        r.msg = msg;
        r.data = null;
        return r;
    }

    /** 失败响应（自定义错误码和消息） */
    public static <T> R<T> fail(Integer code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        r.data = null;
        return r;
    }
}
