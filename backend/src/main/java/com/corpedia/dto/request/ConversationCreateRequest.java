package com.corpedia.dto.request;

/**
 * 新建会话请求体：POST /api/conversations。
 */
public record ConversationCreateRequest(
        String title
) {
}