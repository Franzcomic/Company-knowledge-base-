package com.corpedia.common;

public enum ResultCode {
    OK(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),           // 已登录但无权限（区别于 401 未登录：前端不跳登录）
    NOT_FOUND(404),
    ERROR(500);

    private final int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}