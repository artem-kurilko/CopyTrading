package com.copytrading.sources.binance.futuresleaderboard.model.response.trader.performance;

import com.copytrading.sources.binance.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderPerformanceResponse extends ResponseEntity {
    @Getter
    @Setter
    private TraderPerformance data;

    public TraderPerformanceResponse(String code, String message, String messageDetail, TraderPerformance data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        TraderPerformanceResponse performanceResponse =
                new TraderPerformanceResponse(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(performanceResponse).toString(2);
    }
}
