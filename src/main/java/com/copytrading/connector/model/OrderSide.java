package com.copytrading.connector.model;

import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;

import static java.lang.Double.parseDouble;

public enum OrderSide {
    BUY,
    SELL;

    public static OrderSide getPositionSide(PositionData positionData) {
        double entry = parseDouble(positionData.getEntryPrice());
        double mark = parseDouble(positionData.getMarkPrice());
        double unrealized = parseDouble(positionData.getUnrealizedProfit());
        if ((mark > entry && unrealized > 0) || (mark < entry && unrealized < 0))
            return OrderSide.BUY;
        else
            return OrderSide.SELL;
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

    public static String getOppositeSide(PositionDto positionDto) {
        OrderSide side = getPositionSide(positionDto);
        if (side.equals(BUY))
            return SELL.name();
        else return BUY.name();
    }
}
