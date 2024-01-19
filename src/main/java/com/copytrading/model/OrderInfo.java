package com.copytrading.model;

import lombok.Data;

import java.util.List;

/**
 * Order info entity is used to work with emulating leaderboard orders.
 */
@Data
public class OrderInfo {
    private String traderId;
    private List<String> binanceModelOrders;
}
