package com.copytrading.connector.model;

public enum OrderSide {
    BUY,
    SELL;

    public static OrderSide getOppositeSide(OrderSide side) {
        if (side.equals(BUY))
            return SELL;
        else return BUY;
    }
}
