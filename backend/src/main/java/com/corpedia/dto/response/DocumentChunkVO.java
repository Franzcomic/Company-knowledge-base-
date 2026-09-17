package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 文档分块项（GET /api/documents/{id}/chunks）。
 * similarity 在无检索 query 时不可得，返回 null；前端展示可显示 "-"。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentChunkVO(Integer chunkIndex, String content, Double similarity) {
}
