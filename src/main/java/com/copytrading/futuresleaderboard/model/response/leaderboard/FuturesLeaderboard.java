package com.copytrading.futuresleaderboard.model.response.leaderboard;

import com.copytrading.copytradingleaderboard.model.response.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FuturesLeaderboard extends ResponseEntity {
    @Getter
    @Setter
    private List<Leader> data;

    public FuturesLeaderboard(String code, String message, String messageDetail, List<Leader> data, boolean success) {
        super(code, message, messageDetail, success);
        this.data = data;
    }

    @Override
    public String toString() {
        FuturesLeaderboard futuresLeaderboard =
                new FuturesLeaderboard(super.getCode(), super.getMessage(), super.getMessageDetail(), data, super.isSuccess());
        return new JSONObject(futuresLeaderboard).toString(2);
    }
}
