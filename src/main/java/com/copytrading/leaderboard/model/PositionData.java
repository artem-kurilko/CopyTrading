package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionData {
    private String id;
    private String symbol;
    private String collateral;
    private String positionAmount;
    private String entryPrice;
    private String unrealizedProfit;
    private String cumRealized;
    private String askNotional;
    private String bidNotional;
    private String notionalValue;
    private String markPrice;
    private int leverage;
    private boolean isolated;
    private String isolatedWallet;
    private String adl;
    private String positionSide;
    private String breakEvenPrice;
}
