package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyTrader {
    private String leadPortfolioId;
    private String nickname;
    private String avatarUrl;
    private int currentCopyCount;
    private int maxCopyCount;
    private double roi;
    private double pnl;
    private double aum;
    private double mdd;
    private String apiKeyTag;
    private double sharpRatio;
    private List<ChartItem> chartItems;
}
