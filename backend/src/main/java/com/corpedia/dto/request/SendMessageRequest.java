package com.corpedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 提问请求体：POST /api/messages。
 */
public record SendMessageRequest(
        @NotNull(message = "conversationId 不能为空") Long conversationId,
        @NotBlank(message = "问题内容不能为空") String content
) {
}