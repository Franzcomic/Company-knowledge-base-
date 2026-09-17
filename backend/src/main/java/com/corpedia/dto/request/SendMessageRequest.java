package com.corpedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 提问请求体：POST /api/messages。
 */
public record SendMessageRequest(
        @NotNull(message = "conversationId 不能为空") Long conversationId,
        @NotBlank(message = "问题内容不能为空") @Size(max = 2000, message = "问题最多 2000 字") String content
) {
}
