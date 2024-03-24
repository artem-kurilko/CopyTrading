package com.copytrading.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniPosition {
    private String id;
    private Exchange exchange;
    private String traderId;
    private String symbol;
    private Double entryPrice;
    private Double markPrice;
    private Double pnl;
    private Double sz;
    private Long uTime;
    private Long cTime;
}
