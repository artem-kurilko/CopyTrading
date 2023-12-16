package com.copytrading.leaderboard.copytrading.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardParams {
    private int pageNumber;
    private int pageSize;
    private String timeRange;
    private FilterType dataType;
    private boolean favoriteOnly;
    private boolean hideFull;
    private String nickName;
    private OrderSort order;
}
