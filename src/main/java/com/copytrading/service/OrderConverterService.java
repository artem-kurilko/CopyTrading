package com.copytrading.service;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.copytrading.connector.config.BinanceConfig.futuresClient;

public class OrderConverterService {
    private static final HashMap<String, Integer> symbolTickSizeMap = getTickSizes();

    public static LinkedHashMap<String, Object> getMarketOrderParams(String symbol, String side, double amount) {
        int symbolPrecision = symbolTickSizeMap.get(symbol);
        double quantity = round(amount, symbolPrecision);

        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", Double.toString(quantity));
        params.put("type", OrderType.MARKET.name());
        return params;
    }

    public static LinkedHashMap<String, Object> getOrderParams(String symbol, String side, double amount, String price, OrderType orderType) {
        int symbolPrecision = symbolTickSizeMap.get(symbol);
        double quantity = round(amount, symbolPrecision);

        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", Double.toString(quantity));
        params.put("price", price);
        params.put("type", orderType.name());
        params.put("timeInForce", TimeInForce.GTC.name());
        return params;
    }

    private static HashMap<String, Integer> getTickSizes() {
        HashMap<String, Integer> roundValues = new HashMap<>();
        String res = new BinanceConnector(futuresClient()).exchangeInfo();
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

    private static double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
