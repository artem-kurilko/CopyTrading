package com.copytrading.leaderboard.copytrading.model.response.leaderboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartItem {
    private double value;
    private String dataType;
    private long dateTime;
}