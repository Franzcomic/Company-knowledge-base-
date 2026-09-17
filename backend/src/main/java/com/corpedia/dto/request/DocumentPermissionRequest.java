package com.corpedia.dto.request;

/**
 * 修改文档权限/所属部门（PUT /api/documents/{id}/permission）。
 * permissionLevel 必传（PUBLIC/DEPT/CONFIDENTIAL）；departmentId 可空=全司。
 */
public record DocumentPermissionRequest(
        String permissionLevel,
        Long departmentId
) {
}
