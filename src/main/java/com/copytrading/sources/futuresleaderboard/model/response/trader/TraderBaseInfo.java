package com.copytrading.sources.futuresleaderboard.model.response.trader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderBaseInfo {
    private String nickName;
    private boolean positionShared;
    private boolean deliveryPositionShared;
    private int followingCount;
    private int followerCount;
    private String introduction;
}
