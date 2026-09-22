package com.corpedia.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息（表 message）。USER/ASSISTANT 两条一组；assistant 行含来源 JSON、相似度、是否定答。
 */
@Data
@TableName("message")
public class Message {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话 id。 */
    private Long conversationId;

    /** 角色：USER / ASSISTANT。 */
    private String role;                // USER / ASSISTANT

    /** 消息正文。 */
    private String content;

    /** 引用来源 JSON: [{"documentId","title","chunkId","similarity"}]，仅 assistant 行。 */
    private String sources;             // JSON: [{"documentId","title","chunkId","similarity"}]

    /** 最高相似度，仅 assistant 行。 */
    private Double similarity;

    /** 是否定答：1 已回答 / 0 拒答 / null(User 行)。 */
    private Integer answered;           // 1 已回答 / 0 拒答 / null(User 行)

    /** 阶段5: RAG 生成耗时(ms)，仅 ASSISTANT 行记录，用于 avgResponseMs。 */
    private Long responseMs;            // 阶段5: RAG 生成耗时(ms)，仅 ASSISTANT 行记录，用于 avgResponseMs

    /** 创建时间。 */
    private LocalDateTime createdAt;
}