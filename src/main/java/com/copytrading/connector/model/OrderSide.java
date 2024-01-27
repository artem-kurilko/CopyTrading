package com.copytrading.connector.model;

public enum OrderSide {
    BUY,
    SELL;

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
