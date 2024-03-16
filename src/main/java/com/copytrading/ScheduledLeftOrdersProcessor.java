package com.copytrading;

import com.copytrading.service.LeadTraderDatabaseService;

import java.util.List;

/**
 * It's async class to proceed left orders, when lead trader doesn't show his positions no more, but we already copied them.
 */
public class ScheduledLeftOrdersProcessor {
    private final boolean isProd;

    public ScheduledLeftOrdersProcessor(boolean isProd) {
        this.isProd = isProd;
    }

    public void proceedLeftOrders() {
        LeadTraderDatabaseService db = new LeadTraderDatabaseService(isProd);
        List<String> leftOrders =  db.getLeftOrders();
        if (leftOrders.isEmpty()) {
            System.out.println("nothing");
        }
    }
}
