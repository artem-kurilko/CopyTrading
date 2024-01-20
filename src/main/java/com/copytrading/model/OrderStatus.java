package com.copytrading.model;

import com.copytrading.connector.model.OrderDto;

public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED,
    EXPIRED,
    EXPIRED_IN_MATCH;

    public static boolean checkIfOrderIsActive(OrderDto orderDto) {
        return orderDto.getStatus().equals(NEW.name()) || orderDto.getStatus().equals(PARTIALLY_FILLED.name());
    }
}
