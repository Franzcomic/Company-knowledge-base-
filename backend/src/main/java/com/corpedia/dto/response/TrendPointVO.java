package com.corpedia.dto.response;

import java.time.LocalDate;

/**
 * 趋势单日点：{ date, total }。
 */
public record TrendPointVO(LocalDate date, long total) {
}