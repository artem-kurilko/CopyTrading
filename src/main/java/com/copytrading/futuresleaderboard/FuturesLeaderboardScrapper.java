package com.copytrading.futuresleaderboard;

import com.copytrading.futuresleaderboard.model.request.*;
import com.copytrading.futuresleaderboard.model.response.leaderboard.FuturesLeaderboard;
import com.copytrading.futuresleaderboard.model.response.position.TraderPositions;
import com.copytrading.futuresleaderboard.model.response.trader.TraderInfo;
import com.copytrading.futuresleaderboard.model.response.trader.performance.TraderPerformanceResponse;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class FuturesLeaderboardScrapper {
    private static final FuturesLeaderboardAPI client = getFuturesLeaderboardClient();

    public static FuturesLeaderboard futuresLeaderboard(PeriodType period, StatisticsType type) throws IOException {
        LeaderboardParams param = LeaderboardParams.builder()
                .isShared(true)
                .periodType(period)
                .statisticsType(type)
                .build();
        Call<FuturesLeaderboard> response = client.futuresLeaderboard(param);
        return response.execute().body();
    }

    public static TraderInfo getTradersBaseInfo(String encryptedUid) throws IOException {
        Call<TraderInfo> response = client.tradersBaseInfo(new TraderId(encryptedUid));
        return response.execute().body();
    }

    public static TraderPerformanceResponse getTraderPerformance(String encryptedUid) throws IOException {
        Call<TraderPerformanceResponse> response = client.traderPerformance(new TradePerformanceParams(encryptedUid));
        return response.execute().body();
    }

    /**
     * Returns trader's active positions
     * This method requires authorization and cannot be proceeded
     * @deprecated Because authorization is needed for this request
     *
     * @param encryptedUid trader id
     * @return trader's positions
     * @throws IOException if exception occurs
     */
    public static TraderPositions getTraderPositions(String encryptedUid) throws IOException {
        Call<TraderPositions> response = client.tradersPositions(new TradePerformanceParams(encryptedUid));
        System.out.println(response.execute());
        return null;
    }

    private static String getLink(String id) {
        return "https://www.binance.com/en/futures-activity/leaderboard/user/um?encryptedUid=" + id;
    }

    private static FuturesLeaderboardAPI getFuturesLeaderboardClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.binance.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(FuturesLeaderboardAPI.class);
    }

}
