package com.copytrading.connector.model;

/**
 * Trailing stop order - allows to lock in profits, as percentage or fixed amount, stop price adjusts if price goes right
 * TWAP (time-weighted-average-price) - divides order or parts and spread them evenly over a chosen period.
 * REVERSE - allows to change order from short to long or otherwise simultaneously
 */
public enum OrderType {
    LIMIT,
    MARKET,
    STOP,
    STOP_MARKET, // once stop price triggered order is executed as market
    TAKE_PROFIT,
    TAKE_PROFIT_MARKET,
    TRAILING_STOP_MARKET;
}
