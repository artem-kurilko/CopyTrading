package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.leaderboard.copytrading.model.FilterType;
import com.copytrading.leaderboard.copytrading.model.TimeRange;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.connector.config.BinanceConfig.testClient;
import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.service.OrderConverter.convertOrderParams;
import static com.copytrading.util.ConfigUtils.PARSE_POSITIONS_DELAY;

/**
 * Entry point of application.
 * @author Kurilko Artemii
 * @version 1.0.0
 */

/**
 * TODO: протестироваь трейдеров за разный отрезок времени и может ещё по roi и pnl смотреть
 */
@Slf4j
public class CopyTradingApplication {
    private static final BinanceConnector client = new BinanceConnector(testClient());
    private static final int partitions = 3; // amount of traders to follow and divide balance equally
    private static HashMap<String, Double> tradersBalance = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Running... " + new Date());
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        tradersBalance = initBalance(ids);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (String id : ids)
                            emulateOrders(id);
                        checkOrdersStatus();
                    } catch (Exception e) {
                        executorService.shutdown();
                        e.printStackTrace();
                    }
                }, 0, PARSE_POSITIONS_DELAY, TimeUnit.MILLISECONDS);
    }

    private static HashMap<String, Double> initBalance(List<String> tradersIds) {
        HashMap<String, Double> initializedBalance = new HashMap<>();
        final String baseAsset = "USDT";
        JSONArray balance = new JSONArray(client.balance());
        for (int i = 0; i < balance.length(); i++) {
            JSONObject currencyBalance = balance.getJSONObject(i);
            if (currencyBalance.getString("asset").equals(baseAsset)) {
                double available = currencyBalance.getDouble("availableBalance");
                double partitionBalance = available / partitions;
                tradersIds.forEach(id -> initializedBalance.put(id, partitionBalance));
                return initializedBalance;
            }
        }
        throw new RuntimeException("INIT BALANCE EXCEPTION. Traders ids: " + tradersIds + "\nBalance: " + client.balance());
    }

    /**
     * Emulates trader's active orders
     * @param id trader's id
     * @throws IOException instance
     */
    private synchronized static void emulateOrders(String id) throws IOException {
        List<PositionData> activeOrders = activePositions(id).getData();
        double balance = tradersBalance.get(id);

        System.out.println("Trader Id: " + id);
        System.out.println("Balance: " + balance);
        System.out.println();

        int ordersSize = activeOrders.size();
        if (ordersSize != 0) {
            // if active orders <=3 order price will be 1/3 of the balance,
            // else price will be proportional to the size
            double partitionBalance =
                    ordersSize <= 3 ? balance / 3 : balance / ordersSize;
            for (PositionData order : activeOrders) {
                LinkedHashMap<String, Object> params = convertOrderParams(order, partitionBalance);
                params.forEach((x, y) -> System.out.println(x + " " + y));
                String res = client.placeOrder(params);
                balance -= partitionBalance;
                log.info("Placed order: " + res);
                System.out.println("Updated balance: " + balance);
            }
            tradersBalance.put(id, balance);
        }
    }

    /**
     * Checks active orders, if lead trader executed or cancelled them
     */
    private static void checkOrdersStatus() {
        // do some
    }

}
