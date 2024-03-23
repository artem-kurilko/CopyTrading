package com.copytrading;

import com.copytrading.service.OrdersStateDatabaseService;

/**
 * Paper trading testnet.
 * @author Artemii Lepshokov
 * @version 1.0
 */
public class PaperTestnetRunner {
    private static final boolean mode = false;
    private static final OrdersStateDatabaseService db = new OrdersStateDatabaseService(mode);

    public static void main(String[] args) {
        runAlgorithm();
    }

    public static void runAlgorithm() {
        // write main algorithm
        // write methods to emulate and execute
        // work with db:
        // saving to active orders
        // saving to history



    }

}
