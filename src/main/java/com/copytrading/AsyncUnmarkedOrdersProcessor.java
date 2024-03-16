package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.service.LeadTraderDatabaseService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * It's async class to proceed left orders, when lead trader doesn't show his positions no more, but we already copied them.
 */
public class AsyncUnmarkedOrdersProcessor {
    private final BinanceConnector client;
    private final LeadTraderDatabaseService db;

    private static final int delay = 5;
    private static final int waitNewUnmarkedOrdersDelayMillis = 5000;

    public AsyncUnmarkedOrdersProcessor(boolean isProd) {
        client = new BinanceConnector(isProd);
        db = new LeadTraderDatabaseService(isProd);
    }

    /**
     * Logic:
     * - if top 10 traders with open positions have these symbols than mark to them
     * - if not and positions have positive pnl, then execute now
     * - if not and positions have negative pnl, but other trader's positions have the same side, wait until pnl gets better
     * - if not and positions have negative pnl, and other trader's positions have other side than execute on negative pnl :(
     */
    public void proceedLeftOrders() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            List<String> unmarkedOrders =  db.getUnmarkedOrders();
            // if there are no left orders then sleep
            if (unmarkedOrders.isEmpty()) {
                try {
                    Thread.sleep(waitNewUnmarkedOrdersDelayMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            // check if top 20 traders have similar positions
            System.out.println("Rest of the logic");

            },0, delay, TimeUnit.SECONDS);
    }
}
