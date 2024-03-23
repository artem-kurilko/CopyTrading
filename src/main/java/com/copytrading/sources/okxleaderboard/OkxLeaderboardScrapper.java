package com.copytrading.sources.okxleaderboard;

import com.copytrading.sources.okxleaderboard.model.*;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;

public class OkxLeaderboardScrapper {
    private static final String baseUrl = "https://www.okx.com/priapi/v5/ecotrade/public/";
    private static final OkxLeaderboardAPI client = getOkxLeaderboardClient();

    public static List<LeadTrader> tradersLeaderboard(FilterType type, int size) throws IOException {
        String dataVersion = "20240323234800";
        Call<Response<Leaderboard>> apiCall = client.leaderboard(dataVersion, 0, 1, type, size, System.currentTimeMillis());
        Response<Leaderboard> response = apiCall.execute().body();
        assert response != null;
        return response.getData().get(0).getRanks();
    }

    public static LeadTraderPerformance tradersPerformance(String traderId, TimePeriod period) throws IOException {
        Call<Response<LeadTraderPerformance>> apiCall = client.getPortfolioDetail(period.getValue(), traderId, String.valueOf(System.currentTimeMillis()));
        Response<LeadTraderPerformance> response = apiCall.execute().body();
        assert response != null;
        return response.getData().get(0);
    }

    public static List<PositionInfo> activePositions(String traderId, InstType instType) throws IOException {
        Call<Response<PositionInfo>> apiCall = client.activePositions(instType, traderId, String.valueOf(System.currentTimeMillis()));
        Response<PositionInfo> response = apiCall.execute().body();
        assert response != null;
        return response.getData();
    }

    public static List<PositionInfo> positionHistory(String traderId, InstType instType, int size) throws IOException {
        Call<Response<PositionInfo>> apiCall = client.positionsHistory(instType, traderId, size, String.valueOf(System.currentTimeMillis()));
        Response<PositionInfo> response = apiCall.execute().body();
        assert response != null;
        return response.getData();
    }

    public static String getLink(String id) {
        return "https://www.okx.com/copy-trading/account/" + id + "?tab=swap";
    }

    private static OkxLeaderboardAPI getOkxLeaderboardClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(OkxLeaderboardAPI.class);
    }

}
