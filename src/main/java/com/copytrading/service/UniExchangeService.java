package com.copytrading.service;

import com.copytrading.model.Exchange;
import com.copytrading.model.UniFilterType;
import com.copytrading.model.UniPosition;
import com.copytrading.model.UniTimeRange;
import com.copytrading.sources.copytradingleaderboard.model.request.FilterType;
import com.copytrading.sources.copytradingleaderboard.model.request.TimeRange;
import com.copytrading.sources.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.sources.futuresleaderboard.model.request.PeriodType;
import com.copytrading.sources.futuresleaderboard.model.request.StatisticsType;
import com.copytrading.sources.futuresleaderboard.model.response.leaderboard.Leader;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;
import com.copytrading.sources.okxleaderboard.model.LeadTrader;
import com.copytrading.sources.okxleaderboard.model.PositionInfo;

import java.io.IOException;
import java.util.List;

import static com.copytrading.model.Exchange.BINANCE_LEADERBOARD;
import static com.copytrading.sources.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.sources.copytradingleaderboard.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getTraderPositions;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.validFuturesLeaderboard;
import static com.copytrading.sources.okxleaderboard.OkxLeaderboardScrapper.currentPositions;
import static com.copytrading.sources.okxleaderboard.OkxLeaderboardScrapper.tradersLeaderboard;

public class UniExchangeService {

    public List<String> getTopTraders(UniFilterType filter, UniTimeRange uniTimeRange, int num, Exchange exchange) throws IOException {
        return switch (exchange) {
            case BINANCE_LEADERBOARD -> {
                PeriodType period = switch (uniTimeRange) {
                    case WEEK -> PeriodType.WEEKLY;
                    case MONTH -> PeriodType.MONTHLY;
                    case ALL -> PeriodType.ALL;
                };
                StatisticsType type;
                if (filter.equals(UniFilterType.COPIER_PNL)) {
                    type = StatisticsType.PNL;
                    yield validFuturesLeaderboard(period, type, num).stream().map(Leader::getEncryptedUid).toList();
                } else {
                    throw new IllegalArgumentException("BINANCE LEADERBOARD FILTER EXCEPTION: " + filter);
                }
            }
            case BINANCE_COPYTRADING -> {
                TimeRange timeRange = switch (uniTimeRange) {
                    case WEEK -> TimeRange.D7;
                    case MONTH -> TimeRange.D30;
                    case ALL -> TimeRange.D90;
                };
                FilterType filterType = switch (filter) {
                    case AUM -> FilterType.AUM;
                    case COPIER_PNL -> FilterType.COPIER_PNL;
                };
                yield getTradersIds(num, timeRange, filterType);
            }
            case OKX -> {
                com.copytrading.sources.okxleaderboard.model.FilterType filterType = switch (filter) {
                    case AUM -> com.copytrading.sources.okxleaderboard.model.FilterType.aum;
                    case COPIER_PNL -> com.copytrading.sources.okxleaderboard.model.FilterType.followTotalPnl;
                };
                yield tradersLeaderboard(filterType, num).stream().map(LeadTrader::getUniqueName).toList();
            }
        };
    }

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
                .exchange(BINANCE_LEADERBOARD)
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
