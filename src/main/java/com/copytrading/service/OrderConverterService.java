package com.copytrading.service;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import com.copytrading.exception.InsufficientMarginException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class OrderConverterService {
    private static final HashMap<String, Integer> symbolTickSizeMap = getTickSizes();

    public static LinkedHashMap<String, Object> getMarketOrderParams(String symbol, String side, double amount) {
        LinkedHashMap<String, Object> params = getBaseParams(symbol, side, amount);
        params.put("type", OrderType.MARKET.name());
        return params;
    }

    public static LinkedHashMap<String, Object> getOrderParams(String symbol, String side, double amount, String price, OrderType orderType) {
        LinkedHashMap<String, Object> params = getBaseParams(symbol, side, amount);
        params.put("price", price);
        params.put("type", orderType.name());
        params.put("timeInForce", TimeInForce.GTC.name());
        return params;
    }

    private static LinkedHashMap<String, Object> getBaseParams(String symbol, String side, double amount) {
        String validSymbol = symbol;
        if (validSymbol.contains("_")) {
            validSymbol = symbol.split("_")[0];
        }
        int symbolPrecision = symbolTickSizeMap.get(validSymbol);
        double quantity = round(amount, symbolPrecision);
        if (quantity == 0) {
            throw new InsufficientMarginException("INSUFFICIENT MARGIN FOR " + symbol);
        }
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", Double.toString(Math.abs(quantity)));
        return params;
    }

    private static HashMap<String, Integer> getTickSizes() {
        HashMap<String, Integer> roundValues = new HashMap<>();
        String res = new BinanceConnector(true).exchangeInfo();
        JSONArray symbols = new JSONObject(res).getJSONArray("symbols");
        for (int i = 0; i < symbols.length(); i++) {
            JSONObject asset = symbols.getJSONObject(i);
            String symbol = asset.getString("symbol");
            if (symbol.contains("_"))
                continue;
            int round = asset.getInt("quantityPrecision");
            roundValues.put(symbol, round);
        } return roundValues;
    }

    public static double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
