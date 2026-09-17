package com.corpedia.ai;

import java.util.List;

/**
 * 一次问答的结果：答案 / 引用来源 / 是否定答 / 最大相似度。
 */
public record ChatResult(
        String content,
        List<RetrievedChunk> sources,
        boolean answered,
        double similarity
) {
}