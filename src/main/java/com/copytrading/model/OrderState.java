package com.copytrading.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderState {
    private String traderId;
    private String symbol;
    private PositionSide side;
    private Integer leverage;
    private Double entryPrice;
    private Double size;
    private Double leadTraderEntryPrice;
    private Double closePrice;
    private Double closePnl;
    private Long updateTime;
    private Long time;
}
