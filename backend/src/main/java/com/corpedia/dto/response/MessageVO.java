package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史消息返回体（会话历史 / 消息详情）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageVO(
        Long id,
        String role,              // USER / ASSISTANT
        String content,
        List<SourceVO> sources,   // 仅 ASSISTANT 行
        Boolean answered,         // 仅 ASSISTANT 行：true 已回答 / false 拒答
        LocalDateTime createdAt
) {
}