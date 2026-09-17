package com.corpedia.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建知识库：POST /api/kb
 */
public record KbCreateRequest(
        @NotBlank(message = "知识库名称不能为空") String name,
        Long departmentId,                // 可空=全公司
        String permissionLevel,           // PUBLIC/DEPT/CONFIDENTIAL，缺省 PUBLIC
        String description
) {
}
