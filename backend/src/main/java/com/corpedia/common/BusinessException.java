package com.corpedia.common;

/**
 * 业务异常：携带 ResultCode 与提示信息，由 GlobalExceptionHandler 统一映射为 HTTP 状态码。
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}