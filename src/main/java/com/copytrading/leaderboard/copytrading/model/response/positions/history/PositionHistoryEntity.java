package com.copytrading.leaderboard.copytrading.model.response.positions.history;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionHistoryEntity {
    private String indexValue;
    private int total;
    private List<PositionHistoryData> list;
}
