package com.corpedia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 统计总览（GET /api/stats/overview）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StatsOverviewVO(
        long total,                // 问题总数（USER 消息数）
        long answered,             // 已回答数（ASSISTANT answered=1）
        long notAnswered,          // 拒答数（ASSISTANT answered=0）
        double hitRate,            // 命中率 answered/total
        Double avgResponseMs,      // 平均响应耗时(ms)，无数据为 null
        Double satisfaction,       // 满意率 UP/(UP+DOWN)，无评价为 null
        List<TrendPointVO> trend   // 每日问题趋势
) {
}