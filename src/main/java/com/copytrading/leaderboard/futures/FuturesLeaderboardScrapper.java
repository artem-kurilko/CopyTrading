package com.copytrading.leaderboard.futures;

import com.copytrading.leaderboard.futures.model.LeaderboardParams;
import com.copytrading.leaderboard.futures.model.PeriodType;
import com.copytrading.leaderboard.futures.model.StatisticsType;
import com.copytrading.leaderboard.futures.model.TraderPositionsParams;
import com.copytrading.leaderboard.futures.model.response.FuturesLeaderboard;
import com.copytrading.leaderboard.futures.model.response.TraderPositions;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

import static com.copytrading.util.ConfigUtils.getHeaders;

//TODO: look trade type perpetual, options and what are the difference
public class FuturesLeaderboardScrapper {
    private static final FuturesLeaderboardAPI client = getFuturesLeaderboardClient();

    private static FuturesLeaderboardAPI getFuturesLeaderboardClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.binance.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(FuturesLeaderboardAPI.class);
    }

    @SneakyThrows
    public static void main(String[] args) {
        LeaderboardParams param = LeaderboardParams.builder()
                .isShared(true)
                .periodType(PeriodType.MONTHLY)
                .statisticsType(StatisticsType.PNL)
                .build();
        TraderPositionsParams positionsParams = TraderPositionsParams
                .builder()
                .encryptedUid("DE501A9139BAB9E5F3A77F0BE0AB96BB")
                .build();
        TraderPositions body = getTraderPositions(positionsParams);
        System.out.println(body);
    }

    public static FuturesLeaderboard futuresLeaderboard(PeriodType period, StatisticsType type) throws IOException {
        LeaderboardParams param = LeaderboardParams.builder()
                .isShared(true)
                .periodType(period)
                .statisticsType(type)
                .build();
        Call<FuturesLeaderboard> response = client.futuresLeaderboard(param);
        return response.execute().body();
    }

    /**
     * Returns trader's active positions
     * @deprecated
     * This method requires authorization and cannot be proceeded
     * @param params {@link TraderPositionsParams} instance
     * @return trader's positions
     * @throws IOException if exception occurs
     */
    @Deprecated
    public static TraderPositions getTraderPositions(TraderPositionsParams params) throws IOException {
        Call<TraderPositions> response = client.tradersPositions(params, getHeaders());
        System.out.println(response.execute());
        return null;
    }

}
