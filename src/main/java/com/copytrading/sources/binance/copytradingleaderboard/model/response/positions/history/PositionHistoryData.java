package com.copytrading.sources.binance.copytradingleaderboard.model.response.positions.history;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionHistoryData {
    private long id;
    private String symbol;
    private String type;
    private long opened;
    private long closed;
    private double avgCost;
    private double avgClosePrice;
    private double closingPnl;
    private double maxOpenInterest;
    private double closedVolume;
    private String isolated;
    private PositionSide side;
    private String status;
    private long updateTime;
}
