package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史消息返回体（会话历史 / 消息详情）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageVO(
        /** 消息 id。 */
        Long id,
        /** 角色：USER / ASSISTANT。 */
        String role,              // USER / ASSISTANT
        /** 消息正文。 */
        String content,
        /** 引用来源列表，仅 ASSISTANT 行。 */
        List<SourceVO> sources,   // 仅 ASSISTANT 行
        /** 是否定答，仅 ASSISTANT 行：true 已回答 / false 拒答。 */
        Boolean answered,         // 仅 ASSISTANT 行：true 已回答 / false 拒答
        /** 创建时间。 */
        LocalDateTime createdAt
) {
}