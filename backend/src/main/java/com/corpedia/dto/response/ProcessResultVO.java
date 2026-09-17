package com.corpedia.dto.response;

/**
 * 文档处理触发/重试返回体（POST /api/rag/process/{documentId}）。
 */
public record ProcessResultVO(Long documentId, String status) {
}
