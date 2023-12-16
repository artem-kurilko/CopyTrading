package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.OrderSide;
import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.copytrading.util.ConfigUtils.getProperty;

public class BinanceConnector {

    public static void main(String[] args) throws IOException {
        String testnetBaseUrl = "https://testnet.binancefuture.com";
        String testapi = getProperty("testapi");
        String testsecret = getProperty("testsecret");

        UMFuturesClientImpl client = new UMFuturesClientImpl(testapi, testsecret, testnetBaseUrl);

        LinkedHashMap<String,Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol","BTCUSDT");
        parameters.put("side", OrderSide.SELL.name());
        parameters.put("type", OrderType.LIMIT.name());
        parameters.put("timeInForce", TimeInForce.GTC.name());
        parameters.put("quantity", 1);
        parameters.put("price", 40000);

        String result = client.account().newOrder(parameters);
        System.out.println("NEW ORDER: ");
        System.out.println(result);
        System.out.println();

        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        String openOrders = client.account().currentAllOpenOrders(params);
        System.out.println("OPEN ORDERS: ");
        System.out.println(openOrders);
        System.out.println();

        String trades = client.account().accountTradeList(params);
        System.out.println("TRADES:");
        System.out.println(trades);
    }

    private static String jsonPrettify(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(json);
        String prettyJsonString = gson.toJson(jsonElement);
        return prettyJsonString;
    }

    public static void placeOrderFutures() {
//        UMFuturesClientImpl client = new UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        LinkedHashMap<String,Object> parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol","BTCUSDT");
        parameters.put("side", "SELL");
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", 0.01);
        parameters.put("price", 9500);
//
//        String result = client.trade().testNewOrder(parameters);
    }

    private static void convertPosition(PositionData position) {

    }
}
