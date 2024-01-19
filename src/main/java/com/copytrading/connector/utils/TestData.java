package com.copytrading.connector.utils;

import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import com.copytrading.copytradingleaderboard.model.FilterType;
import com.copytrading.copytradingleaderboard.model.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.TraderInfo;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.*;

public class TestData {

    public static PositionData getTestPositionData() throws IOException {
        CopyTradingLeaderboard leaderboard = tradersLeaderboard(TimeRange.D90, FilterType.COPY_COUNT);
        for (TraderInfo trader : leaderboard.getData().getList()) {
            List<PositionData> activeOrders = activePositions(trader.getLeadPortfolioId()).getData();
            if (isPositionsShown(trader.getLeadPortfolioId()) && activeOrders.size() != 0) {
                System.out.println(trader.getLeadPortfolioId());
                return activeOrders.get(0);
            }
        } return null;
    }

    public static LinkedHashMap<String, Object> getTestParams(String symbol, String side, String quantity, String price) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", quantity);
        params.put("price", price);
        params.put("type", OrderType.LIMIT.name());
        params.put("timeInForce", TimeInForce.GTC.name());
        return params;
    }

}
