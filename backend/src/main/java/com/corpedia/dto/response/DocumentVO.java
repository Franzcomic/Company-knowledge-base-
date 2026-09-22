package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 知识文档返回体（上传/列表/详情/状态轮询复用）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentVO(
        /** 文档 id。 */
        Long id,
        /** 所属知识库 id。 */
        Long kbId,
        /** 原始文件名。 */
        String filename,
        /** 文件类型 md/pdf/docx/txt。 */
        String fileType,
        /** 文件大小（字节）。 */
        Long size,
        /** 处理状态：PARSING / READY / FAILED。 */
        String status,            // PARSING / READY / FAILED
        /** 已入库分块数。 */
        Integer chunkCount,
        /** 权限级别。 */
        String permissionLevel,
        /** 创建时间。 */
        LocalDateTime createdAt
) {
}
