package com.corpedia.dto.response;

/**
 * 文档处理触发/重试返回体（POST /api/rag/process/{documentId}）。
 */
public record ProcessResultVO(
        /** 文档 id。 */
        Long documentId,
        /** 触发后的处理状态（一般为 PARSING）。 */
        String status
) {
}
