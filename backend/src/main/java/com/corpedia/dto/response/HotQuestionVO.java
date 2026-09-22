package com.corpedia.dto.response;

/**
 * 热门问题：{ question, count }。
 */
public record HotQuestionVO(
        /** 问题原文。 */
        String question,
        /** 出现次数。 */
        long count
) {
}