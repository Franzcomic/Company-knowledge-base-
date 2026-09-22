package com.corpedia.dto.response;

/**
 * 角色列表项（GET /api/roles）。
 */
public record RoleVO(
        /** 角色 id。 */
        Long id,
        /** 角色编码。 */
        String code,
        /** 角色展示名。 */
        String name
) {
}
