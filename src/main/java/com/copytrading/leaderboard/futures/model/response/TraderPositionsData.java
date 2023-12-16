package com.copytrading.leaderboard.futures.model.response;

import lombok.Data;

import java.util.List;

@Data
public class TraderPositionsData {
    private List<Position> otherPositionRetList;
    private int[] updateTime;
    private long updateTimeStamp;
}
