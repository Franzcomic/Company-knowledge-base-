package com.corpedia.dto.request;

/**
 * 更新知识库：PUT /api/kb/{id}
 */
public record KbUpdateRequest(
        String name,
        Long departmentId,
        String permissionLevel,
        String description
) {
}
