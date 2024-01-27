package com.copytrading.connector.utils;

import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;

import java.util.LinkedHashMap;

public class OrderDataUtils {

    public static LinkedHashMap<String, Object> getMarketParams(String symbol, String side, String quantity) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", quantity);
        params.put("type", OrderType.MARKET.name());
        return params;
    }

    public static LinkedHashMap<String, Object> getParams(String symbol, String side, String quantity, String price, OrderType orderType) {
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", quantity);
        params.put("price", price);
        params.put("type", orderType.name());
        params.put("timeInForce", TimeInForce.GTC.name());
        return params;
    }

}
