package com.copytrading.copytradingleaderboard.model.response.positions.history;

import com.copytrading.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionHistory extends ResponseEntity {
    @Getter
    @Setter
    private PositionHistoryEntity data;

    public PositionHistory(String code, String message, String messageDetail, PositionHistoryEntity data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        PositionHistory positionHistory =
                new PositionHistory(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(positionHistory).toString(2);
    }
}
