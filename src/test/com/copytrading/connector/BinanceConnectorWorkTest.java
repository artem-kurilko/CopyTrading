package com.copytrading.connector;

import com.copytrading.copytradingleaderboard.model.FilterType;
import com.copytrading.copytradingleaderboard.model.LeaderboardParams;
import com.copytrading.copytradingleaderboard.model.OrderSort;
import com.copytrading.copytradingleaderboard.model.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.TraderInfo;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import static com.copytrading.connector.config.BinanceConfig.testClient;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.*;
import static com.copytrading.service.OrderConverter.convertOrderParams;

public class BinanceConnectorWorkTest {
    private final BinanceConnector connector = new BinanceConnector(testClient());

    @Test
    public void testNewOrder() throws IOException {
        double balance = 200;
        LinkedHashMap<String,Object> params = convertOrderParams(getOrderExample(), balance);
        String order = connector.placeOrder(params);
        System.out.println(order);
    }

    public PositionData getOrderExample() throws IOException {
        LeaderboardParams params = LeaderboardParams.builder()
                .pageNumber(1)
                .pageSize(18)
                .timeRange(TimeRange.D90.value)
                .dataType(FilterType.ROI)
                .favoriteOnly(false)
                .hideFull(false)
                .nickName("")
                .order(OrderSort.DESC)
                .build();
        CopyTradingLeaderboard leaderboard = tradersLeaderboard(params);
        List<TraderInfo> traders = leaderboard.getData().getList();
        for (TraderInfo trader : traders) {
            List<PositionData> activeOrders = activePositions(trader.getLeadPortfolioId()).getData();
            if (isPositionsShown(trader.getLeadPortfolioId()) && activeOrders.size() != 0) {
                return activeOrders.get(0);
            }
        } return null;
    }
}
