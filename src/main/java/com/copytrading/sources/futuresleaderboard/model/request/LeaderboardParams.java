package com.copytrading.sources.futuresleaderboard.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardParams {
    private boolean isShared;
    private PeriodType periodType;
    private StatisticsType statisticsType;
    private final String tradeType = "PERPETUAL";
}
