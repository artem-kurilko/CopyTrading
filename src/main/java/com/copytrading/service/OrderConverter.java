package com.copytrading.service;

import com.copytrading.connector.model.OrderSide;
import com.copytrading.connector.model.OrderType;
import com.copytrading.connector.model.TimeInForce;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;

import java.util.LinkedHashMap;

import static com.copytrading.connector.model.OrderSide.BUY;
import static com.copytrading.connector.model.OrderSide.SELL;
import static java.lang.Double.parseDouble;

/**
 * Java class is used to convert order's from one format to another to send api request.
 */
public class OrderConverter {

    public static LinkedHashMap<String,Object> convertOrderParams(PositionData data) {
        OrderSide side;
        if (parseDouble(data.getEntryPrice()) < parseDouble(data.getBreakEvenPrice()))
            side = SELL;
        else side = BUY;

        LinkedHashMap<String,Object> params = new LinkedHashMap<>();
        params.put("symbol", data.getSymbol());
        params.put("side", side);
        params.put("positionSide", data.getPositionSide());
        params.put("quantity", data.getPositionAmount());
        params.put("stopPrice", data.getBreakEvenPrice());
        params.put("price", data.getMarkPrice());
        params.put("type", OrderType.LIMIT.name());
        params.put("timeInForce", TimeInForce.GTC.name());

//        params.put("activationPrice", data.getEntryPrice());
        return params;
    }

}
