package com.copytrading.sources.futuresleaderboard.model.request;

public class TradePerformanceParams extends TraderId {
    private final String tradeType = "PERPETUAL";

    public TradePerformanceParams(String encryptedUid) {
        super(encryptedUid);
    }

}
