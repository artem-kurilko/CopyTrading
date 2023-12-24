package com.copytrading.connector;

import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.service.OrderConverter.convertOrderParams;
import static java.lang.Double.parseDouble;

//POST /fapi/v1/leverage - set leverage
public class BinanceConnectorTest {

    @SneakyThrows
    public static void main(String[] args) {
        String traderId = "3705565653582929153";
        ActivePositions actOrders = activePositions(traderId);
        LinkedHashMap<String, Object> parameters = convertOrderParams(actOrders.getData().get(0));
        System.out.println(parameters);
        System.out.println(placeOrder(parameters));
    }

    public static JSONObject placeOrder(LinkedHashMap<String, Object> parameters) throws IOException {
        Response<String> response = testClient().placeOrder(parameters).execute();
        System.out.println(response.body());
        return new JSONObject(response.body());
    }

    @SneakyThrows
    private static BinanceTestService testClient() {
        String fbaseUrl = "https://testnet.binancefuture.com";
        String baseUrl = "https://fapi.binance.com";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(BinanceTestService.class);
    }

    interface BinanceTestService {
        @POST("/fapi/v1/order/test")
        Call<String> placeOrder(@Body LinkedHashMap<String, Object> params);
    }
}