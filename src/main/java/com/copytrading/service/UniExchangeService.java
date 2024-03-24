package com.copytrading.service;

import com.copytrading.model.Exchange;
import com.copytrading.model.UniPosition;
import com.copytrading.sources.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;
import com.copytrading.sources.okxleaderboard.model.PositionInfo;

import java.io.IOException;
import java.util.List;

import static com.copytrading.sources.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getTraderPositions;
import static com.copytrading.sources.okxleaderboard.OkxLeaderboardScrapper.currentPositions;

public class UniExchangeService {

    public List<UniPosition> getPositions(String traderId, Exchange exchange) throws IOException {
        return switch (exchange) {
            case OKX -> currentPositions(traderId).stream().map(this::formatUniPosition).toList();
            case BINANCE_LEADERBOARD -> getTraderPositions(traderId).getData().getOtherPositionRetList().stream().map(this::formatUniPosition).toList();
            case BINANCE_COPYTRADING -> activePositions(traderId).getData().stream().map(this::formatUniPosition).toList();
        };
    }

    public UniPosition formatUniPosition(PositionInfo position) {
        return UniPosition.builder()
                .exchange(Exchange.OKX)
                .symbol(position.getInstId())
                .build();
    }

    public UniPosition formatUniPosition(Position position) {
        return UniPosition.builder()
                .exchange(Exchange.BINANCE_LEADERBOARD)
                .symbol(position.getSymbol())
                .build();
    }

    public UniPosition formatUniPosition(PositionData position) {
        return UniPosition.builder()
                .exchange(Exchange.BINANCE_COPYTRADING)
                .symbol(position.getSymbol())
                .build();
    }

}
