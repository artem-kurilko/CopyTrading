package com.copytrading.leaderboard;

import com.copytrading.copytradingleaderboard.model.*;
import com.copytrading.copytradingleaderboard.model.response.details.TraderData;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.TraderInfo;

import java.io.IOException;
import java.util.List;

import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.*;

public class CopyLeaderboardAPITest {

    public static void main(String[] args) throws IOException {
        List<String> ids = getTradersIds(3, TimeRange.D30, FilterType.COPIER_PNL);
        ids.forEach(System.out::println);
    }

    public void getPositionsHistoryTest() throws IOException {
        String portfolioId = "3701555442111522817";
        PositionHistoryParams params = PositionHistoryParams.builder()
                .pageNumber(1)
                .pageSize(10)
                .portfolioId(portfolioId)
                .build();
//        System.out.println(positionsHistory(params));
    }

    public void getLeaderboardTest() throws IOException {
        System.out.println("COPIER PNL");
        CopyTradingLeaderboard leaderboard = tradersLeaderboard(TimeRange.D90, FilterType.COPIER_PNL);
        showLeaderboard(leaderboard);
        System.out.println();
    }

    public void showLeaderboard(CopyTradingLeaderboard leaderboard) throws IOException {
        List<TraderInfo> traders = leaderboard.getData().getList();
        int i = 0;
        for (TraderInfo trader : traders) {
            String portfolioId = trader.getLeadPortfolioId();
            TraderData traderData = getTraderDetails(portfolioId).getData();
            if (traderData.isPositionShow()) {
                String url = String.format("https://www.binance.com/en/copy-trading/lead-details/%s?timeRange=90D", portfolioId);
                System.out.println(url);
                i++;
                if (i==3)
                    break;
            }
        }
    }
}
