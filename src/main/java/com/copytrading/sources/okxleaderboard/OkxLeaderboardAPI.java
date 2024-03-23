package com.copytrading.sources.okxleaderboard;

import com.copytrading.sources.okxleaderboard.model.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OkxLeaderboardAPI {

    @GET("follow-rank")
    Call<Response<Leaderboard>> leaderboard(@Query("dataVersion") String dataVersion,
                                            @Query("fullState") int fullState,
                                            @Query("start") int start,
                                            @Query("type") FilterType type,
                                            @Query("size") int size,
                                            @Query("t") long time);

    @GET("trade-data")
    Call<Response<LeadTraderPerformance>> getPortfolioDetail(@Query("latestNum") int period, @Query("uniqueName") String traderId, @Query("t") String time);

    @GET("position-summary")
    Call<Response<PositionInfo>> activePositions(@Query("instType") InstType instType, @Query("uniqueName") String traderId, @Query("t") String time);

    @GET("position-history")
    Call<Response<PositionInfo>> positionsHistory(@Query("instType") InstType instType,
                                                  @Query("uniqueName") String traderId,
                                                  @Query("size") int size,
                                                  @Query("t") String time);

}
