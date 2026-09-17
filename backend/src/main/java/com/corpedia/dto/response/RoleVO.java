package com.corpedia.dto.response;

/**
 * 角色列表项（GET /api/roles）。
 */
public record RoleVO(Long id, String code, String name) {
}
