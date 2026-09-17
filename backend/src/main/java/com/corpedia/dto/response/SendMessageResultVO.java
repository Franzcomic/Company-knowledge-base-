package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 提问返回体：POST /api/messages（便于前端即时渲染，无需二次查历史）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMessageResultVO(
        Long messageId,
        String content,
        List<SourceVO> sources,
        boolean answered
) {
}