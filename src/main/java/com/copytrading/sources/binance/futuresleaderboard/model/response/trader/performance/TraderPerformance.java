package com.copytrading.sources.binance.futuresleaderboard.model.response.trader.performance;

import lombok.Data;

import java.util.List;

@Data
public class TraderPerformance {
    private List<PerformanceDto> performanceRetList;
    private long lastTradeTime;
}
