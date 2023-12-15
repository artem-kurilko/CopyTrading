package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyTraderDetails extends ResponseEntity {
    @Getter
    @Setter
    private CopyTraderData data;

    public CopyTraderDetails(String code, String message, String messageDetail, CopyTraderData data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        CopyTraderDetails traderDetails =
                new CopyTraderDetails(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderDetails).toString(2);
    }
}
