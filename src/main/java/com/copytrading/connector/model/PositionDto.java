package com.copytrading.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionDto {
    private String symbol;
    private Double leverage;
    private Double notional;
    private String isolatedWallet;
    private String breakEvenPrice;
    private String isolatedMargin;
    private String positionSide;
    private boolean isolated;
    private String liquidationPrice;
    private String maxNotionalValue;
    private long updateTime;
    private Double entryPrice;
    private Double positionAmt;
    private String adlQuantile;
    private Double markPrice;
    private Double unRealizedProfit;
    private boolean isAutoAddMargin;
    private String marginType;
}
