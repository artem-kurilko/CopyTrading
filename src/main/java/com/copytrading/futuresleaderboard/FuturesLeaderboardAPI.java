package com.copytrading.futuresleaderboard;

import com.copytrading.futuresleaderboard.model.request.LeaderboardParams;
import com.copytrading.futuresleaderboard.model.request.TradePerformanceParams;
import com.copytrading.futuresleaderboard.model.request.TraderId;
import com.copytrading.futuresleaderboard.model.response.leaderboard.FuturesLeaderboard;
import com.copytrading.futuresleaderboard.model.response.position.TraderPositions;
import com.copytrading.futuresleaderboard.model.response.trader.TraderInfo;
import com.copytrading.futuresleaderboard.model.response.trader.performance.TraderPerformanceResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FuturesLeaderboardAPI {

    @POST("/bapi/futures/v2/public/future/leaderboard/getLeaderboardRank")
    Call<FuturesLeaderboard> futuresLeaderboard(@Body LeaderboardParams params);

    @POST("/bapi/futures/v2/public/future/leaderboard/getOtherLeaderboardBaseInfo")
    Call<TraderInfo> tradersBaseInfo(@Body TraderId encryptedUid);

    @POST("/bapi/futures/v2/public/future/leaderboard/getOtherPerformance")
    Call<TraderPerformanceResponse> traderPerformance(@Body TradePerformanceParams params);

    @POST("/bapi/futures/v2/private/future/leaderboard/getOtherPosition")
    Call<TraderPositions> tradersPositions(@Body TradePerformanceParams params);
}
