package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 知识文档返回体（上传/列表/详情/状态轮询复用）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentVO(
        Long id,
        Long kbId,
        String filename,
        String fileType,
        Long size,
        String status,            // PARSING / READY / FAILED
        Integer chunkCount,
        String permissionLevel,
        LocalDateTime createdAt
) {
}
