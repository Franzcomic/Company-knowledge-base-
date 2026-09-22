package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档（表 document）。命名 KbDocument 以避免与 Spring AI 的 org.springframework.ai.document.Document 混淆。
 */
@Data
@TableName("document")
public class KbDocument {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属知识库 id。 */
    private Long kbId;

    /** 原始文件名。 */
    private String filename;

    /** 落盘绝对路径。 */
    private String filePath;

    /** 文件内容 SHA-256，用于同库查重。 */
    private String fileHash;            // 文件内容 SHA-256，用于同库查重

    /** 文件类型：md/pdf/docx/txt。 */
    private String fileType;            // md/pdf/docx/txt

    /** 文件大小（字节）。 */
    private Long size;

    /** 处理状态：PARSING / READY / FAILED。 */
    private String status;              // PARSING / READY / FAILED

    /** 已入库分块数。 */
    private Integer chunkCount;

    /** 权限级别，上传时继承所属知识库。 */
    private String permissionLevel;     // 上传时继承所属知识库

    /** 文档级所属部门（上传时快照自知识库；权限设置可独立修改），可空=全司。 */
    private Long departmentId;          // 文档级所属部门（上传时快照自知识库；权限设置可独立修改），可空=全司

    /** 上传人用户 id。 */
    private Long uploadedBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
