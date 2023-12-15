package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceData {
    private String timeRange;
    private double roi;
    private double pnl;
    private double mdd;
    private double winRate;
    private int winOrders;
    private int totalOrders;
}
