package com.copytrading;

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.CopyTradingApplication.log;

public class MainTest {
    private static final int SOCKET_RETRY_COUNT = 2;

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (int i = 0; i < SOCKET_RETRY_COUNT; i++) {
                            try {
                                doSomeShit(2);
                                return;
                            } catch (SocketTimeoutException ex) {
                                ex.printStackTrace();
                                Thread.sleep(20000);
                            }
                        }
                    } catch (Exception e) {
                        executorService.shutdown();
                        log.info("=================================================\n");
                        e.printStackTrace();
                    }
                }, 0, 5, TimeUnit.SECONDS);
    }

    private static void doSomeShit(int n) throws SocketTimeoutException {
        System.out.println("CALLED: " + new Date());
        if (n!=3)
            throw new SocketTimeoutException();
    }
}
