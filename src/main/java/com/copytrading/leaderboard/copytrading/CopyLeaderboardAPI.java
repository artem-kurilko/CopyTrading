package com.copytrading.leaderboard.copytrading;

import com.copytrading.leaderboard.copytrading.model.LeaderboardParams;
import com.copytrading.leaderboard.copytrading.model.PositionHistoryParams;
import com.copytrading.leaderboard.copytrading.model.response.details.TraderDetails;
import com.copytrading.leaderboard.copytrading.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.leaderboard.copytrading.model.response.performance.TraderPerformance;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;
import com.copytrading.leaderboard.copytrading.model.response.positions.history.PositionHistory;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CopyLeaderboardAPI {

    @POST("v1/friendly/future/copy-trade/home-page/query-list")
    Call<CopyTradingLeaderboard> tradersLeaderboard(@Body LeaderboardParams params);

    @GET("v1/friendly/future/copy-trade/lead-portfolio/detail")
    Call<TraderDetails> getPortfolioDetail(@Query("portfolioId") String portfolioId);

    @GET("v1/public/future/copy-trade/lead-portfolio/performance")
    Call<TraderPerformance> tradersPerformance(@Query("portfolioId") String portfolioId, @Query("timeRange") String timeRange);

    @GET("v1/friendly/future/copy-trade/lead-data/positions")
    Call<ActivePositions> activePositions(@Query("portfolioId") String portfolioId);

    @POST("v1/public/future/copy-trade/lead-portfolio/position-history")
    Call<PositionHistory> positionsHistory(@Body PositionHistoryParams positionHistoryParams);

}
