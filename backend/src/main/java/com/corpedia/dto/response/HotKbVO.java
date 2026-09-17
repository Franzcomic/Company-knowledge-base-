package com.corpedia.dto.response;

/**
 * 热门知识库：{ kbName, count }。
 */
public record HotKbVO(String kbName, long count) {
}