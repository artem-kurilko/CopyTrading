package com.copytrading.leaderboard.copytrading;

import com.copytrading.leaderboard.copytrading.model.*;
import com.copytrading.leaderboard.copytrading.model.response.ResponseEntity;
import com.copytrading.leaderboard.copytrading.model.response.details.TraderDetails;
import com.copytrading.leaderboard.copytrading.model.response.leaderboard.CopyTradingLeaderboard;
import com.copytrading.leaderboard.copytrading.model.response.leaderboard.TraderInfo;
import com.copytrading.leaderboard.copytrading.model.response.performance.TraderPerformance;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import com.copytrading.leaderboard.copytrading.model.response.positions.history.PositionHistory;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
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

    private static CopyLeaderboardAPI getCopyLeaderboardClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(CopyLeaderboardAPI.class);
    }

    @SneakyThrows
    public static void main(String[] args) {
        LeaderboardParams params = LeaderboardParams.builder()
                .pageNumber(1)
                .pageSize(18)
                .timeRange(TimeRange.D90.value)
                .dataType(FilterType.AUM)
                .favoriteOnly(false)
                .hideFull(false)
                .nickName("")
                .order(OrderSort.DESC)
                .build();
        CopyTradingLeaderboard leaderboard = tradersLeaderboard(params);
        List<TraderInfo> traders = leaderboard.getData().getList();
        int i = 0;
        for (TraderInfo trader : traders) {
            if (isPositionsShown(trader.getLeadPortfolioId())) {
                System.out.println(new JSONObject(trader).toString(2));
                System.out.println();
                i++;
                if (i==5)
                    break;
            }
        }
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

    public static PositionHistory positionsHistory(PositionHistoryParams positionHistoryParams) throws IOException {
        Call<PositionHistory> apiCall = client.positionsHistory(positionHistoryParams);
        PositionHistory response = apiCall.execute().body();
        assert response != null;
        checkResponseStatus(response);
        return response;
    }

    private static boolean isPositionsShown(String traderId) throws IOException {
        var details = getTraderDetails(traderId);
        return details.getData().isPositionShow();
    }

    private static <E extends ResponseEntity> void checkResponseStatus(E response) {
        if (!response.isSuccess())
            throw new RuntimeException("Exception while sending request. " + response);
    }

}
