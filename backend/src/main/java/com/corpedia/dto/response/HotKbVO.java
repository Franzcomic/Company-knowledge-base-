package com.corpedia.dto.response;

/**
 * 热门知识库：{ kbName, count }。
 */
public record HotKbVO(
        /** 知识库名称。 */
        String kbName,
        /** 被引用次数。 */
        long count
) {
}