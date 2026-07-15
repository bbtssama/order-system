package com.atguigu.order.common;

/**
 * 自定义业务异常
 * <p>
 * 用于在业务逻辑中抛出可预期的错误（如参数校验失败、资源不存在等），
 * 由 {@link GlobalExceptionHandler} 统一捕获并转换为前端可读的响应。
 * 继承 RuntimeException 以支持事务回滚。
 * </p>
 */
public class BusinessException extends RuntimeException {

    /** 错误码，默认 500 */
    private final Integer code;

    /**
     * 构造业务异常（默认错误码 500）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造业务异常（指定错误码）
     *
     * @param code 错误码（如 400 参数错误、403 无权限、404 资源不存在）
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
