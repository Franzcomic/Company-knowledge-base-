package com.corpedia.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * 统一返回体 { code, msg, data }，与前端 Axios 拦截器匹配。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Result<T>(int code, String msg, T data) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.OK.getCode(), "success", data);
    }

    public static Result<Void> ok() {
        return new Result<>(ResultCode.OK.getCode(), "success", null);
    }

    public static <T> Result<T> fail(ResultCode rc, String msg) {
        return new Result<>(rc.getCode(), msg, null);
    }

    public static <T> Result<T> fail(ResultCode rc) {
        return new Result<>(rc.getCode(), rc.name(), null);
    }

    public boolean isSuccess() {
        return Objects.equals(code, ResultCode.OK.getCode());
    }
}