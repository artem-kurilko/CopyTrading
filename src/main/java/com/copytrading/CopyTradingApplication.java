package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.copytradingleaderboard.model.FilterType;
import com.copytrading.copytradingleaderboard.model.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.model.BaseAsset;
import com.copytrading.model.OrderInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.connector.config.BinanceConfig.testClient;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.service.OrderConverter.convertOrderParams;
import static com.copytrading.util.ConfigUtils.PARSE_POSITIONS_DELAY;

/**
 * Entry point of application.
 * @author Kurilko Artemii
 * @version 1.0.0
 */

/**
 * TODO:
 *  - копирование напрямую с лидерборда плохо, потому что открытые позиции не у топ трейдеров, например пнл 1го открытого 180 000, пнл 1го закрытого 1 200 000
 *  - протестироваь трейдеров за разный отрезок времени и может ещё по roi и pnl смотреть
 *
 */
@Slf4j
public class CopyTradingApplication {
    private static final BinanceConnector client = new BinanceConnector(testClient());
    private static final int partitions = 3; // amount of traders to follow and divide balance equally
    private static HashMap<String, Double> tradersBalance = new HashMap<>(); // available balance for copying each trader
    private static final List<OrderInfo> ordersStorage = new ArrayList<>();

    public static void mafin(String[] args) throws IOException {
        System.out.println("Running... " + new Date());
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        tradersBalance = initBalance(ids, USDT);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (String id : ids)
                            checkOrdersStatus(id);
                    } catch (Exception e) {
                        executorService.shutdown();
                        e.printStackTrace();
                    }
                }, 0, PARSE_POSITIONS_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Initialized balance evenly divided between {@link partitions} size traders.
     * @param tradersIds lead trader portfolio id (necessary to have show positions true)
     * @param baseAsset asset balance
     * @return hashmap of trader's id and balance to make trades.
     */
    private static HashMap<String, Double> initBalance(List<String> tradersIds, BaseAsset baseAsset) {
        HashMap<String, Double> initializedBalance = new HashMap<>();
        JSONArray balance = new JSONArray(client.balance());
        for (int i = 0; i < balance.length(); i++) {
            JSONObject currencyBalance = balance.getJSONObject(i);
            if (currencyBalance.getString("asset").equals(baseAsset.name())) {
                double available = currencyBalance.getDouble("availableBalance");
                double partitionBalance = available / partitions;
                tradersIds.forEach(id -> initializedBalance.put(id, partitionBalance));
                return initializedBalance;
            }
        }
        throw new RuntimeException("INIT BALANCE EXCEPTION. Traders ids: " + tradersIds + "\nBalance: " + client.balance());
    }

    @SneakyThrows
    public static void main(String[] args) {
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        ids.forEach(System.out::println);
    }

    /**
     * Checks storage orders, trader leaderboard orders, if he has new then copy.
     * @param id traders leaderboard id
     * @throws IOException if exception occurs
     */
    private static void checkOrdersStatus(String id) throws IOException {
        // check that orders still active in storage
        ObjectMapper mapper = new ObjectMapper();
        List<String> openOrders = Arrays.asList(mapper.readValue(client.openOrders(), String[].class));
        OrderInfo orderInfo = ordersStorage.stream().filter(info -> info.getTraderId().equals(id)).findFirst().get();

        // сделать работу с балансом

        // check that trader's active orders are the same as mine, and if he has any i executed then don't copy,
        // and check if he has new and we have balance then emulate
        openOrders.forEach(System.out::println);
    }

    /**
     * Emulates trader's active orders
     * @param id trader's id
     * @throws IOException instance
     */
    private static void emulateOrders(String id) throws IOException {
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

}
