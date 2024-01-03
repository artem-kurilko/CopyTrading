package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.MarginType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Binance service connector.
 */
public class BinanceConnector {
    private final UMFuturesClientImpl client;

    public BinanceConnector(UMFuturesClientImpl client) {
        this.client = client;
    }

    public String placeOrder(LinkedHashMap<String, Object> parameters) {
        String response = client.account().newOrder(parameters);
        return new JSONObject(response).toString(2);
    }

    public String positionInfo() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().positionInformation(parameters);
        return new JSONArray(result).toString(2);
    }

    public String balance() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String response = client.account().futuresAccountBalance(parameters);
        return new JSONArray(response).toString(2);
    }

    /**
     * Returns info with current symbol's leverage
     * @param symbol currency pair
     * @return string value
     */
    public String leverage(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String response = client.account().positionInformation(parameters);
        return new JSONArray(response).toString(2);
    }

    public String cancelOrder(String symbol, String orderId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("orderId", orderId);
        String result = client.account().cancelOrder(parameters);
        return new JSONObject(result).toString(2);
    }

    public String cancelAll(String symbol) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        String result = client.account().cancelAllOpenOrders(params);
        return new JSONObject(result).toString(2);
    }

    public String openOrders() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().currentAllOpenOrders(parameters);
        return new JSONArray(result).toString(2);
    }

    public String allOrders(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = client.account().allOrders(parameters);
        return new JSONArray(result).toString(2);
    }

    public String changeLeverage(String symbol, int leverage) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("leverage", leverage);
        String result = client.account().changeInitialLeverage(parameters);
        return new JSONObject(result).toString(2);
    }

    public String changeMarginType(String symbol, MarginType type) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("marginType", type);
        String response = client.account().changeMarginType(params);
        JSONObject respObject = new JSONObject(response);
        if (Objects.equals(respObject.getString("msg"), "success"))
            return respObject.toString(2);
        else
            throw new RuntimeException("Exception while changing margin type.");
    }

}
