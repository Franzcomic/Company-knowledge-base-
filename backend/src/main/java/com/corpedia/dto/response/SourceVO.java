package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 答案来源片段（前端 SourceCard 用）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SourceVO(
        /** 来源文档 id。 */
        Long documentId,
        /** 来源文档标题。 */
        String title,
        /** 命中的分块 id。 */
        String chunkId,
        /** 该分块相似度得分。 */
        double similarity
) {
}