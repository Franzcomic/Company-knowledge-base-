package com.corpedia.dto.response;

/**
 * 对齐前端 UserInfo 字段（/auth/login 返回的 user 与 /auth/me 直接用）。
 */
public record UserInfoVO(
        /** 用户 id。 */
        Long id,
        /** 登录名。 */
        String username,
        /** 真实姓名。 */
        String realName,
        /** 部门 id。 */
        Long departmentId,
        /** 角色 id。 */
        Long roleId,
        /** 角色编码。 */
        String roleCode,
        /** 角色名称。 */
        String roleName,
        /** 邮箱。 */
        String email,
        /** 手机号。 */
        String phone
) {
}