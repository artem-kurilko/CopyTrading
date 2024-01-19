package com.copytrading.futuresleaderboard.model.response.trader.performance;

import com.copytrading.futuresleaderboard.model.request.PeriodType;
import com.copytrading.futuresleaderboard.model.request.StatisticsType;
import lombok.Data;

@Data
public class PerformanceDto {
    private PeriodType periodType;
    private StatisticsType statisticsType;
    private double value;
    private int rank;
}
