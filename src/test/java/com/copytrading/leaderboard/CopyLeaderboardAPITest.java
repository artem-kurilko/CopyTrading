package java.com.copytrading.leaderboard;

import com.copytrading.leaderboard.copytrading.model.*;

import java.io.IOException;

import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.positionsHistory;

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

    public void getLeaderboardTest() {
        String portfolioId = "3701555442111522817";
        LeaderboardParams params = LeaderboardParams.builder()
                .pageNumber(1)
                .pageSize(18)
                .timeRange(TimeRange.D7.value)
                .dataType(FilterType.SHARP_RATIO)
                .favoriteOnly(false)
                .hideFull(false)
                .nickName("")
                .order(OrderSort.DESC)
                .build();
    }
}
