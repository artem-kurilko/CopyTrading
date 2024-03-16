package com.copytrading.sources.binance.copytradingleaderboard.model.response.positions.active;

import com.copytrading.sources.binance.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivePositions extends ResponseEntity {
    @Getter
    @Setter
    private List<PositionData> data;

    public ActivePositions(String code, String message, String messageDetail, List<PositionData> data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        ActivePositions traderPositions =
                new ActivePositions(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderPositions).toString(2);
    }
}
