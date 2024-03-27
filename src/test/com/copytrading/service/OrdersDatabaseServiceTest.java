package com.copytrading.service;

import com.copytrading.model.PositionSide;

import java.util.List;
import java.util.UUID;

public class OrdersDatabaseServiceTest {
    private static final TestnetDatabaseService db = new TestnetDatabaseService(false);

    public static void main(String[] args) {
        List<OrderState> orders = List.of(
                OrderState.builder()
                        .traderId(UUID.randomUUID().toString())
                        .side(PositionSide.SHORT)
                        .build(),
                OrderState.builder()
                        .traderId(UUID.randomUUID().toString())
                        .side(PositionSide.LONG)
                        .build()
        );
//        db.saveOrderState(orders);
//        db.retrieveOrdersState();
        System.out.println(db.retrieveTradersIds());
    }

}
