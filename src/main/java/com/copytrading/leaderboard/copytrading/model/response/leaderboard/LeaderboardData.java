package com.copytrading.leaderboard.copytrading.model.response.leaderboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaderboardData {
    private String indexValue;
    private int total;
    private List<TraderInfo> list;
}
