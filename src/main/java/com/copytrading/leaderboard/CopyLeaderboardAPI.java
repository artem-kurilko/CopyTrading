package com.copytrading.leaderboard;

import com.copytrading.leaderboard.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CopyLeaderboardAPI {

    @POST("v1/friendly/future/copy-trade/home-page/query-list")
    Call<CopyTradingLeaderboard> tradersLeaderboard(@Body LeaderboardParams params);

    @GET("v1/friendly/future/copy-trade/lead-portfolio/detail")
    Call<CopyTraderDetails> getPortfolioDetail(@Query("portfolioId") String portfolioId);

    @GET("v1/public/future/copy-trade/lead-portfolio/performance")
    Call<CopyTraderPerformance> tradersPerformance(@Query("portfolioId") String portfolioId, @Query("timeRange") String timeRange);

    @GET("v1/friendly/future/copy-trade/lead-data/positions")
    Call<CopyTraderPositions> activePositions(@Query("portfolioId") String portfolioId);

}
