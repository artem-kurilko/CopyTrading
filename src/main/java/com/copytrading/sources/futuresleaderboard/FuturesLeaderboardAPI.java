package com.copytrading.sources.futuresleaderboard;

import com.copytrading.sources.futuresleaderboard.model.request.LeaderboardParams;
import com.copytrading.sources.futuresleaderboard.model.request.TradePerformanceParams;
import com.copytrading.sources.futuresleaderboard.model.request.TraderId;
import com.copytrading.sources.futuresleaderboard.model.response.trader.TraderInfo;
import com.copytrading.sources.futuresleaderboard.model.response.trader.performance.TraderPerformanceResponse;
import com.copytrading.sources.futuresleaderboard.model.response.leaderboard.FuturesLeaderboard;
import com.copytrading.sources.futuresleaderboard.model.response.position.TraderPositions;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.util.Map;

public interface FuturesLeaderboardAPI {

    @POST("/bapi/futures/v2/public/future/leaderboard/getLeaderboardRank")
    Call<FuturesLeaderboard> futuresLeaderboard(@Body LeaderboardParams params);

    @POST("/bapi/futures/v2/public/future/leaderboard/getOtherLeaderboardBaseInfo")
    Call<TraderInfo> tradersBaseInfo(@Body TraderId encryptedUid);

    @POST("/bapi/futures/v2/public/future/leaderboard/getOtherPerformance")
    Call<TraderPerformanceResponse> traderPerformance(@Body TradePerformanceParams params);

    @POST("/bapi/futures/v2/private/future/leaderboard/getOtherPosition")
    Call<TraderPositions> tradersPositions(@Body TradePerformanceParams params, @HeaderMap Map<String, String> headers);

}
