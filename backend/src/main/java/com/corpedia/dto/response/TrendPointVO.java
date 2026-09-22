package com.corpedia.dto.response;

import java.time.LocalDate;

/**
 * 趋势单日点：{ date, total }。
 */
public record TrendPointVO(
        /** 日期（yyyy-MM-dd）。 */
        LocalDate date,
        /** 当日问题数。 */
        long total
) {
}