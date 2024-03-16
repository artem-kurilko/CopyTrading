package com.copytrading.sources.binance.copytradingleaderboard.model.response.positions.active;

import com.copytrading.model.PositionSide;
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
    private PositionSide positionSide;
    private String breakEvenPrice;
}
