package com.copytrading.sources.binance.futuresleaderboard.model.response.trader.performance;

import com.copytrading.sources.binance.futuresleaderboard.model.request.PeriodType;
import com.copytrading.sources.binance.futuresleaderboard.model.request.StatisticsType;
import lombok.Data;

@Data
public class PerformanceDto {
    private PeriodType periodType;
    private StatisticsType statisticsType;
    private double value;
    private int rank;
}
