package com.copytrading.leaderboard.futures.model.response;

import com.copytrading.leaderboard.copytrading.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderPositions extends ResponseEntity {
    @Getter
    @Setter
    private TraderPositionsData data;

    public TraderPositions(String code, String message, String messageDetail, TraderPositionsData data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        TraderPositions positions =
                new TraderPositions(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(positions).toString(2);
    }
}
