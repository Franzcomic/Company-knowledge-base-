package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 文档分块项（GET /api/documents/{id}/chunks）。
 * similarity 在无检索 query 时不可得，返回 null；前端展示可显示 "-"。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentChunkVO(
        /** 分块序号（从 0 开始）。 */
        Integer chunkIndex,
        /** 分块正文。 */
        String content,
        /** 相似度得分（仅检索上下文可得，否则 null）。 */
        Double similarity
) {
}
