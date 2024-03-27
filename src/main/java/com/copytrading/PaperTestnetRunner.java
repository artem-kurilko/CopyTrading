package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.model.Exchange;
import com.copytrading.model.OrderSide;
import com.copytrading.model.UniPosition;
import com.copytrading.service.TestnetDatabaseService;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Paper trading testnet.
 * @author Artemii Lepshokov
 * @version 1.0
 */
public class PaperTestnetRunner {
    private static final boolean mode = false;
    private static final TestnetDatabaseService db = new TestnetDatabaseService(mode);
    private static final BinanceConnector client = new BinanceConnector(mode);

    // storage
    private static HashMap<String, Double> markPrices = new HashMap<>();

    public static void main(String[] args) {
        ScheduledExecutorService updateMarkPriceScheduler = Executors.newSingleThreadScheduledExecutor();
        updateMarkPriceScheduler.scheduleAtFixedRate(() -> markPrices = client.getMarkPrices(), 0, 20, TimeUnit.SECONDS);

        runAlgorithm();
    }

    public static void runAlgorithm() {
        // Main algorithm
        // we get for example 30 top traders from each source, and save them to db
        // algorithm is the same as main bot, the difference each trade has 10$ per margin, and


        // I need to store - trader dto, active orders dto, orders history dto

        // write main algorithm
        // write methods to emulate and execute
        // work with db:
        // saving to active orders
        // saving to history



    }

    private static void copyPosition() {

    }

    private static void executePosition() {

    }

    private static double calculatePnl(UniPosition uniPosition) {
        String symbol = uniPosition.getSymbol();
        double curPrice = markPrices.get(symbol);
        double entryPrice = uniPosition.getEntryPrice();
        double lever = uniPosition.getLeverage();
        OrderSide side = uniPosition.getSide();


        if (side.equals(OrderSide.BUY)) {

        } else {

        }


        return 0;
    }

    @Data
    @Builder
    static class LeadTraderDto {
        private String leaderId;
        private Exchange exchange;
        private Double pnl;
        private Double roi;
        private Double aum;
        private int totalTrades;
        private boolean isPositionShared;
        private long uTime;
        private long cTime;
    }

}
