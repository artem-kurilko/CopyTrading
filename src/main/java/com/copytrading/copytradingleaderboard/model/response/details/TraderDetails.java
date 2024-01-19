package com.copytrading.copytradingleaderboard.model.response.details;

import com.copytrading.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderDetails extends ResponseEntity {
    @Getter
    @Setter
    private TraderData data;

    public TraderDetails(String code, String message, String messageDetail, TraderData data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        TraderDetails traderDetails =
                new TraderDetails(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderDetails).toString(2);
    }
}
