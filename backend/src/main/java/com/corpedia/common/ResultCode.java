package com.corpedia.common;

/**
 * 业务返回码枚举，与统一返回体 Result.code 对齐。
 */
public enum ResultCode {
    /** 成功。 */
    OK(200),
    /** 请求参数错误。 */
    BAD_REQUEST(400),
    /** 未登录或登录过期。 */
    UNAUTHORIZED(401),
    /** 已登录但无权限（区别于 401 未登录：前端不跳登录）。 */
    FORBIDDEN(403),           // 已登录但无权限（区别于 401 未登录：前端不跳登录）
    /** 资源不存在。 */
    NOT_FOUND(404),
    /** 服务器内部错误。 */
    ERROR(500);

    private final int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}