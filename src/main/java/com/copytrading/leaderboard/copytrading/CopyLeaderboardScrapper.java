package com.copytrading.leaderboard.copytrading;

import com.copytrading.leaderboard.copytrading.model.LeaderboardParams;
import com.copytrading.leaderboard.copytrading.model.PositionHistoryParams;
import com.copytrading.leaderboard.copytrading.model.TimeRange;
import com.copytrading.leaderboard.copytrading.model.response.ResponseEntity;
import com.copytrading.leaderboard.copytrading.model.response.details.TraderDetails;
import com.copytrading.leaderboard.copytrading.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.leaderboard.copytrading.model.response.performance.TraderPerformance;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;
import com.copytrading.leaderboard.copytrading.model.response.positions.history.PositionHistory;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

import static com.copytrading.leaderboard.copytrading.model.BinanceConstants.baseUrl;

//TODO: - in validate method, what is status active, is it active trader or just online
public class CopyLeaderboardScrapper {
    private static final CopyLeaderboardAPI client = getBinanceCopyTradingClient();

    @SneakyThrows
    public static void main(String[] args) {
        String portfolioId = "3701555442111522817";
    }

    public static CopyTradingLeaderboard tradersLeaderboard(LeaderboardParams params) throws IOException {
        Call<CopyTradingLeaderboard> apiCall = client.tradersLeaderboard(params);
        CopyTradingLeaderboard response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    public static TraderDetails getTraderDetails(String portfolioId) throws IOException {
        Call<TraderDetails> apiCall = client.getPortfolioDetail(portfolioId);
        TraderDetails response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    public static TraderPerformance traderPerformance(String portfolioId, TimeRange timeRange) throws IOException {
        Call<TraderPerformance> apiCall = client.tradersPerformance(portfolioId, timeRange.value);
        TraderPerformance response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    public static ActivePositions activePositions(String portfolioId) throws IOException {
        Call<ActivePositions> apiCall = client.activePositions(portfolioId);
        ActivePositions response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    public static PositionHistory positionsHistory(PositionHistoryParams positionHistoryParams) throws IOException {
        Call<PositionHistory> apiCall = client.positionsHistory(positionHistoryParams);
        PositionHistory response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    public static boolean isCopyTraderActiveAndShowPositions(String portfolioId) throws IOException {
        TraderDetails traderDetails = getTraderDetails(portfolioId);
        checkResponseStatus(traderDetails);
        return traderDetails.getData().isPositionShow() && traderDetails.getData().getStatus().equals("ACTIVE");
    }

    public static CopyLeaderboardAPI getBinanceCopyTradingClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(CopyLeaderboardAPI.class);
    }

    private static <E extends ResponseEntity> void checkResponseStatus(E response) {
        if (!response.isSuccess())
            throw new RuntimeException("Exception while sending request. " + response);
    }

}
