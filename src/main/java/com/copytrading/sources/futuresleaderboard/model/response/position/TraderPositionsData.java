package com.copytrading.sources.futuresleaderboard.model.response.position;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderPositionsData {
    private List<Position> otherPositionRetList;
    private int[] updateTime;
    private long updateTimeStamp;
}
