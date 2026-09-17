package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 知识库返回体（GET /api/kb 等）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record KbVO(
        Long id,
        String name,
        Long departmentId,
        String permissionLevel,
        String description,
        LocalDateTime createdAt
) {
}
