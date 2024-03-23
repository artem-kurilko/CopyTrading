package com.copytrading.sources.futuresleaderboard.model.response.position;

import com.copytrading.sources.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

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
        return "TraderPositions{" +
                "data=" + data +
                '}';
    }
}
