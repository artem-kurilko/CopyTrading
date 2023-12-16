package java.com.copytrading.leaderboard;

import com.copytrading.leaderboard.copytrading.model.*;
import com.copytrading.leaderboard.copytrading.model.response.details.TraderData;
import com.copytrading.leaderboard.copytrading.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.leaderboard.copytrading.model.response.leaderboard.TraderInfo;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;

import java.io.IOException;
import java.util.List;

import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.*;

public class CopyLeaderboardAPITest {

    public static void main(String[] args) {

    }

    public void getPositionsHistoryTest() throws IOException {
        String portfolioId = "3701555442111522817";
        PositionHistoryParams params = PositionHistoryParams.builder()
                .pageNumber(1)
                .pageSize(10)
                .portfolioId(portfolioId)
                .build();
        System.out.println(positionsHistory(params));
    }

    public void getLeaderboardTest() throws IOException {
//        String portfolioId = "3701555442111522817";
        LeaderboardParams params = LeaderboardParams.builder()
                .pageNumber(1)
                .pageSize(18)
                .timeRange(TimeRange.D30.value)
                .dataType(FilterType.SHARP_RATIO)
                .favoriteOnly(false)
                .hideFull(false)
                .nickName("")
                .order(OrderSort.DESC)
                .build();
        CopyTradingLeaderboard leaderboard = tradersLeaderboard(params);
        List<TraderInfo> traders = leaderboard.getData().getList();
        for (TraderInfo trader : traders) {
            String portfolioId = trader.getLeadPortfolioId();
            TraderData traderData = getTraderDetails(portfolioId).getData();
            if (traderData.isPositionShow()) {
                ActivePositions positions = activePositions(portfolioId);
                if (!positions.getData().isEmpty()) {
                    System.out.println("Name: " + trader.getNickname() + " Id: " + portfolioId + " Is positionsShow: " + traderData.isPositionShow());
                }
            }
        }
    }
}
