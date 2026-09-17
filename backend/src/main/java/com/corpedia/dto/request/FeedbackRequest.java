package com.corpedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 回答评价（POST /api/messages/{messageId}/feedback）：rating=UP/DOWN，reason 可选。
 */
public record FeedbackRequest(
        @NotBlank(message = "rating 不能为空") String rating,
        @Size(max = 255, message = "评价原因最长 255 字") String reason
) {
}
