package com.copytrading.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * Simple entity to track orders for paper trading.
 *
 * @see com.copytrading.TestnetPaperTrading
 */
@Data
@Builder
public class TradeDto {
    @Id
    private String traderId;
    private String symbol;
    private PositionSide side;
    private int leverage;
    private double entryPrice;
    private double size;
    private long updateTime;
    private long time;
}
