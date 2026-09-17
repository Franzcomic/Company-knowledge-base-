package com.corpedia.dto.response;

/**
 * 对齐前端 UserInfo 字段（/auth/login 返回的 user 与 /auth/me 直接用）。
 */
public record UserInfoVO(
        Long id,
        String username,
        String realName,
        Long departmentId,
        Long roleId,
        String roleCode,
        String roleName,
        String email,
        String phone
) {
}