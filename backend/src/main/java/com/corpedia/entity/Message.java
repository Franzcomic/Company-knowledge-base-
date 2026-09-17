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

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private String role;                // USER / ASSISTANT
    private String content;
    private String sources;             // JSON: [{"documentId","title","chunkId","similarity"}]
    private Double similarity;
    private Integer answered;           // 1 已回答 / 0 拒答 / null(User 行)
    private LocalDateTime createdAt;
}