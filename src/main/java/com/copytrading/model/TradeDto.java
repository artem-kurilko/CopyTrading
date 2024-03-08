package com.copytrading.model;

import com.copytrading.TestnetPaperTrading;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Simple entity to track orders for paper trading.
 *
 * @see TestnetPaperTrading
 */
@Data
@Builder
@Document(collection = "TradeDto")
public class TradeDto {
    @Id
    private String traderId;
    private List<TradeDtoData> positions;
}
