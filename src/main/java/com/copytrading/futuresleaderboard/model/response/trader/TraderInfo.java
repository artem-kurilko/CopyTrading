package com.copytrading.futuresleaderboard.model.response.trader;

import com.copytrading.copytradingleaderboard.model.response.ResponseEntity;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

public class TraderInfo extends ResponseEntity {
    @Getter
    @Setter
    private TraderBaseInfo data;

    public TraderInfo(String code, String message, String messageDetail, TraderBaseInfo data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        TraderInfo traderInfo =
                new TraderInfo(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(traderInfo).toString(2);
    }
}
