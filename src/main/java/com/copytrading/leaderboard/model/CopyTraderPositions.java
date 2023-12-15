package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyTraderPositions extends ResponseEntity {
    @Getter
    @Setter
    private List<PositionData> data;

    public CopyTraderPositions(String code, String message, String messageDetail, List<PositionData> data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        CopyTraderPositions traderPositions =
                new CopyTraderPositions(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderPositions).toString(2);
    }
}
