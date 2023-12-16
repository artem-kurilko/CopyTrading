package com.copytrading.leaderboard.futures.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TraderPositionsParams {
    private String encryptedUid;
    private final String tradeType = "PERPETUAL";
}
