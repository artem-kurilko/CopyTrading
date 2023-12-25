package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.MarginType;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

import static com.copytrading.connector.config.BinanceConfig.testClient;
import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.service.OrderConverter.convertOrderParams;

/**
 * Binance service connector.
 */
public class BinanceConnector {
    private final UMFuturesClientImpl client;

    public BinanceConnector(UMFuturesClientImpl client) {
        this.client = client;
    }

    public static void main(String[] args) throws IOException {
        String traderId = "3705565653582929153";
        ActivePositions actOrders = activePositions(traderId);
        LinkedHashMap<String, Object> parameters = convertOrderParams(actOrders.getData().get(1));
        BinanceConnector conn = new BinanceConnector(testClient());

        System.out.println(conn.placeOrder(parameters));
//        System.out.println(conn.positionInfo().toString(2));
    }

    public JSONArray positionInfo() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().positionInformation(parameters);
        return new JSONArray(result);
    }

    public JSONArray balance() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String response = client.account().futuresAccountBalance(parameters);
        return new JSONArray(response);
    }

    public JSONObject placeOrder(LinkedHashMap<String, Object> parameters) {
        String response = client.account().newOrder(parameters);
        JSONObject order = new JSONObject(response);
//        someShit();
//        storeOrderRecord(order);
        return order;
    }

    public JSONObject cancelOrder(String symbol, String orderId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("orderId", orderId);
        String result = client.account().cancelOrder(parameters);
        return new JSONObject(result);
    }

    public JSONArray openOrders(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = client.account().currentAllOpenOrders(parameters);
        return new JSONArray(result);
    }

    public JSONObject changeLeverage(String symbol, int leverage) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("leverage", leverage);
        String result = client.account().changeInitialLeverage(parameters);
        return new JSONObject(result);
    }

    public JSONObject changeMarginType(String symbol, MarginType type) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("marginType", type);
        String response = client.account().changeMarginType(params);
        JSONObject respObject = new JSONObject(response);
        if (Objects.equals(respObject.getString("msg"), "success"))
            return respObject;
        else
            throw new RuntimeException("Exception while changing margin type.");
    }

}
