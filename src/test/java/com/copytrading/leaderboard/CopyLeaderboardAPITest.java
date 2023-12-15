package java.com.copytrading.leaderboard;

import com.copytrading.leaderboard.model.LeaderboardParams;
import com.copytrading.leaderboard.model.OrderSort;
import com.copytrading.leaderboard.model.TimeRange;

public class CopyLeaderboardAPITest {

    public static void main(String[] args) {
        String portfolioId = "3701555442111522817";
        LeaderboardParams params = LeaderboardParams.builder()
                .pageNumber(1)
                .pageSize(18)
                .timeRange(TimeRange.D7.value)
                .dataType("SHARP_RATIO")
                .favoriteOnly(false)
                .hideFull(false)
                .nickName("")
                .order(OrderSort.DESC)
                .build();
    }
}
