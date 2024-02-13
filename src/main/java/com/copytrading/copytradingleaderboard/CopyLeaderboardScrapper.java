package com.copytrading.copytradingleaderboard;

import com.copytrading.copytradingleaderboard.model.request.*;
import com.copytrading.copytradingleaderboard.model.response.ResponseEntity;
import com.copytrading.copytradingleaderboard.model.response.details.TraderDetails;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.copytradingleaderboard.model.response.leaderboard.TraderInfo;
import com.copytrading.copytradingleaderboard.model.response.performance.TraderPerformance;
import com.copytrading.copytradingleaderboard.model.response.positions.active.ActivePositions;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.copytradingleaderboard.model.response.positions.history.PositionHistory;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

public class CopyLeaderboardScrapper {
    private static final String baseUrl = "https://www.binance.com/bapi/futures/";
    private static final CopyLeaderboardAPI client = getCopyLeaderboardClient();

    public static List<String> getTradersIds(int amount, TimeRange timeRange, FilterType filterType) throws IOException {
        List<String> tradersIds = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            CopyTradingLeaderboard leaderboard = tradersLeaderboard(timeRange, filterType, i);
            List<TraderInfo> traders = leaderboard.getData().getList();
            for (TraderInfo trader : traders) {
                if (isPositionsShown(trader.getLeadPortfolioId())) {
                    tradersIds.add(trader.getLeadPortfolioId());
                    amount--;
                    if (amount==0)
                        return tradersIds;
                }
            }
        } throw new RuntimeException("Get traders ids exception.");
    }

    public static CopyTradingLeaderboard tradersLeaderboard(TimeRange timeRange, FilterType filterBy) throws IOException {
        return tradersLeaderboard(timeRange, filterBy, 1);
    }

    public static CopyTradingLeaderboard tradersLeaderboard(TimeRange timeRange, FilterType filterBy, int pageNumber) throws IOException {
        LeaderboardParams params = LeaderboardParams.builder()
                .pageNumber(pageNumber)
                .pageSize(18)
                .timeRange(timeRange.value)
                .dataType(filterBy)
                .favoriteOnly(false)
                .hideFull(false)
                .nickName("")
                .order(OrderSort.DESC)
                .build();
        return tradersLeaderboard(params);
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

        List<PositionData> activePositions = new ArrayList<>();
        List<PositionData> positionData = response.getData();
        for (PositionData position : positionData) {
            if (parseDouble(position.getPositionAmount()) != 0) {
                activePositions.add(position);
            }
        }
        response.setData(activePositions);
        return response;
    }

    public static PositionHistory positionsHistory(String portfolioId, int pageNumber, int pageSize) throws IOException {
        PositionHistoryParams positionHistoryParams = PositionHistoryParams.builder()
                .portfolioId(portfolioId)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .build();
        Call<PositionHistory> apiCall = client.positionsHistory(positionHistoryParams);
        PositionHistory response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    public static boolean isPositionsShown(String traderId) throws IOException {
        var details = getTraderDetails(traderId);
        return details.getData().isPositionShow();
    }

    private static <E extends ResponseEntity> void checkResponseStatus(E response) {
        if (!response.isSuccess())
            throw new RuntimeException("Exception while sending request. " + response);
    }

    public static String getLink(String traderId) {
        return "https://www.binance.com/uk-UA/copy-trading/lead-details/" + traderId + "?timeRange=90D";
    }

    private static CopyLeaderboardAPI getCopyLeaderboardClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(CopyLeaderboardAPI.class);
    }

}
