package com.corpedia.dto.request;

/**
 * 更新用户（PUT /api/users/{id}）：departmentId / roleId / status 均可选，只更新传入字段。
 */
public record UserUpdateRequest(
        Long departmentId,
        Long roleId,
        Integer status
) {
}
