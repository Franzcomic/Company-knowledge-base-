package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 提问返回体：POST /api/messages（便于前端即时渲染，无需二次查历史）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMessageResultVO(
        /** 落库的 assistant 消息 id。 */
        Long messageId,
        /** 生成的回答正文。 */
        String content,
        /** 引用来源列表。 */
        List<SourceVO> sources,
        /** 是否成功定答。 */
        boolean answered
) {
}