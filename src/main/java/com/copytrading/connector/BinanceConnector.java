package com.copytrading.connector;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.copytrading.connector.model.MarginType;
import com.copytrading.connector.model.OrderDto;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Binance service connector.
 */
public class BinanceConnector {
    private static final Gson gson = new Gson();
    private final UMFuturesClientImpl client;

    public BinanceConnector(UMFuturesClientImpl client) {
        this.client = client;
    }

    public OrderDto placeOrder(LinkedHashMap<String, Object> parameters) {
        String response = client.account().newOrder(parameters);
        return gson.fromJson(response, OrderDto.class);
    }

    public String exchangeInfo() {
        String result = client.market().exchangeInfo();
        return new JSONObject(result).toString(2);
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

    public OrderDto getOrder(String symbol, String orderId) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("orderId", orderId);
        String response = client.account().queryOrder(params);
        return gson.fromJson(response, OrderDto.class);
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

    public OrderDto cancelOrder(String symbol, String orderId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("orderId", orderId);
        String result = client.account().cancelOrder(parameters);
        return gson.fromJson(result, OrderDto.class);
    }

    public String cancelAll(String symbol) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        String result = client.account().cancelAllOpenOrders(params);
        return new JSONObject(result).toString(2);
    }

    public List<OrderDto> openOrders() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        String result = client.account().currentAllOpenOrders(parameters);
        return Arrays.asList(gson.fromJson(result, OrderDto[].class));
    }

    public List<OrderDto> allOrders(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = client.account().allOrders(parameters);
        return Arrays.asList(gson.fromJson(result, OrderDto[].class));
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
