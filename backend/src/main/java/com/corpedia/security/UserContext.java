package com.corpedia.security;

/**
 * 当前登录用户上下文（不可变记录）：由 JwtAuthFilter 解析 token 后填充。
 * 经 ThreadLocal（UserContextHolder）在单次请求内传递。
 */
public record UserContext(
        /** 用户 id。 */
        Long userId,
        /** 登录名。 */
        String username,
        /** 部门 id。 */
        Long deptId,
        /** 角色 id。 */
        Long roleId,
        /** 角色编码（权限判定用）。 */
        String roleCode
) {
}