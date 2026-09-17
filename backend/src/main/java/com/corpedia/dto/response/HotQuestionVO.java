package com.corpedia.dto.response;

/**
 * 热门问题：{ question, count }。
 */
public record HotQuestionVO(String question, long count) {
}