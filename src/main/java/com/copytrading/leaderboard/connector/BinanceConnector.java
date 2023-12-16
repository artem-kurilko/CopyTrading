package com.copytrading.leaderboard.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;

import static com.copytrading.leaderboard.copytrading.util.ConfigUtils.getProperty;

public class BinanceConnector {

    public static void main(String[] args) throws IOException {
        String apikey = getProperty("apikey");
        String secretkey = getProperty("secretkey");
        UMFuturesClientImpl client = new UMFuturesClientImpl(apikey, secretkey);
        String result = client.market().time();
        System.out.println(result);
    }

    private static String jsonPrettify(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(json);
        String prettyJsonString = gson.toJson(jsonElement);
        return prettyJsonString;
    }

    public static void placeOrderFutures() {
    }

    private static void convertPosition(PositionData position) {

    }
}
