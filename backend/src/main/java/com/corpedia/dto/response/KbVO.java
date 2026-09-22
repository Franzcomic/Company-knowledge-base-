package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 知识库返回体（GET /api/kb 等）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record KbVO(
        /** 知识库 id。 */
        Long id,
        /** 知识库名称。 */
        String name,
        /** 所属部门 id（可空=全司）。 */
        Long departmentId,
        /** 权限级别 PUBLIC/DEPT/CONFIDENTIAL。 */
        String permissionLevel,
        /** 知识库描述。 */
        String description,
        /** 创建时间。 */
        LocalDateTime createdAt
) {
}
