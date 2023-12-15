package com.copytrading.leaderboard;

import com.copytrading.leaderboard.model.*;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

import static com.copytrading.domain.BinanceConstants.binanceBaseUrl;

//TODO: - in validate method, what is status active, is it active trader or just online
public class BinanceCopyLeaderboardScrapper {
    private static final CopyLeaderboardAPI client = getBinanceCopyTradingClient();

    private static final String positionHistoryApi = binanceBaseUrl + "/v1/public/future/copy-trade/lead-portfolio/position-history"; // post {"pageNumber":1,"pageSize":10,"portfolioId":"3701555442111522817"}

    @SneakyThrows
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
        System.out.println(tradersLeaderboard(params));
    }

    private static CopyTradingLeaderboard tradersLeaderboard(LeaderboardParams params) throws IOException {
        Call<CopyTradingLeaderboard> apiCall = client.tradersLeaderboard(params);
        Response<CopyTradingLeaderboard> response = apiCall.execute();
        return response.body();
    }

    private static CopyTraderDetails getTraderDetails(String portfolioId) throws IOException {
        Call<CopyTraderDetails> apiCall = client.getPortfolioDetail(portfolioId);
        Response<CopyTraderDetails> response = apiCall.execute();
        return response.body();
    }

    private static CopyTraderPerformance traderPerformance(String portfolioId, TimeRange timeRange) throws IOException {
        Call<CopyTraderPerformance> apiCall = client.tradersPerformance(portfolioId, timeRange.value);
        Response<CopyTraderPerformance> response = apiCall.execute();
        return response.body();
    }

    private static CopyTraderPositions activePositions(String portfolioId) throws IOException {
        Call<CopyTraderPositions> apiCall = client.activePositions(portfolioId);
        Response<CopyTraderPositions> response = apiCall.execute();
        return response.body();
    }

    private static boolean isCopyTraderActiveAndShowPositions(String portfolioId) throws IOException {
        CopyTraderDetails traderDetails = getTraderDetails(portfolioId);
        return traderDetails.getData().isPositionShow() && traderDetails.getData().getStatus().equals("ACTIVE");
    }

    private static CopyLeaderboardAPI getBinanceCopyTradingClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(binanceBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(CopyLeaderboardAPI.class);
    }

}
