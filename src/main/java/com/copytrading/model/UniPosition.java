package com.copytrading.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniPosition {
    private String id;
    private String traderId;
    private double entryPrice;
    private Double markPrice;
    private Double pnl;
    private Double sz;
    private long uTime;
    private long cTime;
}
