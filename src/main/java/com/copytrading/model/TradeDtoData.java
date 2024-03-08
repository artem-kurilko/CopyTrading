package com.copytrading.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeDtoData {
    private String id;
    private String symbol;
    private PositionSide side;
    private Integer leverage;
    private double entryPrice;
    private double size;
    private double leadTraderEntryPrice;
    private Double closePrice;
    private Double closePnl;
    private long updateTime;
    private long time;
}
