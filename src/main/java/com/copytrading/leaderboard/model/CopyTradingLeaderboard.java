package com.copytrading.leaderboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyTradingLeaderboard extends ResponseEntity {
    @Getter
    @Setter
    private LeaderboardData data;

    public CopyTradingLeaderboard(String code, String message, String messageDetail, LeaderboardData data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        CopyTradingLeaderboard leaderboard =
                new CopyTradingLeaderboard(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(leaderboard).toString(2);
    }
}
