package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyTraderPerformance extends ResponseEntity {
    @Getter
    @Setter
    private PerformanceData data;

    public CopyTraderPerformance(String code, String message, String messageDetail, PerformanceData data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        CopyTraderPerformance traderPerformance =
                new CopyTraderPerformance(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderPerformance).toString(2);
    }
}
