package com.copytrading.service;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.copytrading.connector.config.BinanceConfig.futuresClient;
import static com.copytrading.connector.model.OrderSide.BUY;
import static com.copytrading.connector.model.OrderSide.SELL;
import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;

/**
 * Java class is used to convert order's from one format to another to send api request.
 */
public class OrderConverter {
    public static HashMap<String, Integer> symbolTickSizeMap = getTickSizes();

    public static LinkedHashMap<String,Object> convertOrderParams(PositionData data, Double balance) {
        double entryPrice = parseDouble(data.getEntryPrice());
        double markPrice = parseDouble(data.getMarkPrice());
        double profit = parseDouble(data.getUnrealizedProfit());
        String symbol = data.getSymbol();
        String side = entryPrice < markPrice && profit > 0 ? BUY.name() : SELL.name();
        double price = side.equals("BUY") ? markPrice * 1.05 : markPrice * 0.95; // if long price 5% higher, if short 5% lower
        int symbolPrecision = symbolTickSizeMap.get(symbol);
        double quantity = round(balance / price, symbolPrecision);

        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("quantity", valueOf(quantity));
        params.put("price", valueOf(round(price, 3)));
        params.put("type", OrderType.LIMIT.name());
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

    public static double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
