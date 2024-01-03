package com.copytrading.service;

import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;

import java.util.LinkedHashMap;

import static com.copytrading.connector.model.OrderSide.BUY;
import static com.copytrading.connector.model.OrderSide.SELL;
import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;

/**
 * Java class is used to convert order's from one format to another to send api request.
 */
public class OrderConverter {

    public static LinkedHashMap<String,Object> convertOrderParams(PositionData data) {
        double entryPrice = parseDouble(data.getEntryPrice());
        double markPrice = parseDouble(data.getMarkPrice());
        double profit = parseDouble(data.getUnrealizedProfit());
        double price = round(parseDouble(data.getEntryPrice()), 2);
        String side = entryPrice < markPrice && profit > 0 ? BUY.name() : SELL.name();

        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", data.getSymbol());
        params.put("side", side);
        params.put("quantity", data.getPositionAmount());
        params.put("price", valueOf(price));
        params.put("type", OrderType.LIMIT.name());
        params.put("timeInForce", TimeInForce.GTC.name());
        return params;
    }

    public static double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
