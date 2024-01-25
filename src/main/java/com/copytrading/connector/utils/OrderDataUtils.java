package com.copytrading.connector.utils;

import com.copytrading.connector.model.OrderSide;
import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.PositionDto;
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

    public static OrderSide getPositionSide(PositionDto positionDto) {
        double entry = positionDto.getEntryPrice();
        double mark = positionDto.getMarkPrice();
        double unrealized = positionDto.getUnRealizedProfit();
        if ((mark > entry && unrealized > 0) || (mark < entry && unrealized < 0))
            return OrderSide.BUY;
        else
            return OrderSide.SELL;
    }

}
