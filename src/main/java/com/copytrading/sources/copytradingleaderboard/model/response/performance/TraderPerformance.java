package com.copytrading.sources.copytradingleaderboard.model.response.performance;

import com.copytrading.sources.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderPerformance extends ResponseEntity {
    @Getter
    @Setter
    private PerformanceData data;

    public TraderPerformance(String code, String message, String messageDetail, PerformanceData data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        TraderPerformance traderPerformance =
                new TraderPerformance(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderPerformance).toString(2);
    }
}
