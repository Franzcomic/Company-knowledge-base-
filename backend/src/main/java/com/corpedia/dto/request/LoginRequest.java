package com.corpedia.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体：POST /api/auth/login。
 */
public record LoginRequest(
        /** 登录用户名，非空。 */
        @NotBlank(message = "用户名不能为空") String username,
        /** 明文密码（仅用于传输，服务端 BCrypt 校验），非空。 */
        @NotBlank(message = "密码不能为空") String password
) {
}