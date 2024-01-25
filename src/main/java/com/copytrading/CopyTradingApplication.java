package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.OrderSide;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.copytradingleaderboard.model.request.FilterType;
import com.copytrading.copytradingleaderboard.model.request.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.model.BaseAsset;
import lombok.SneakyThrows;
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
import static com.copytrading.connector.utils.OrderDataUtils.getMarketParams;
import static com.copytrading.connector.utils.OrderDataUtils.getPositionSide;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.model.OrderStatus.checkIfOrderIsActive;
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
 */
@Slf4j
public class CopyTradingApplication {
    private static final BinanceConnector client = new BinanceConnector(testClient());
    private static final int partitions = 3; // amount of traders to follow and divide balance equally
    private static HashMap<String, Double> balance = new HashMap<>(); // available balance for copying each trader
    private static final HashMap<String, List<OrderDto>> ordersStorage = new HashMap<>(); // stores trader id and copied active orders

    public static void mafin(String[] args) throws IOException {
        System.out.println("Running... " + new Date());
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        balance = initBalance(ids, USDT);

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
     * Initialized balance evenly divided between {@link CopyTradingApplication#partitions} size traders.
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
        // {"code":-4131,"msg":"The counterparty's best price does not meet the PERCENT_PRICE filter limit."}
        executeOrder(null, null);
    }

    /**
     * Checks storage orders, trader leaderboard orders, if he has new then copy.
     * @param id traders leaderboard id
     * @throws IOException if exception occurs
     */
    private static void checkOrdersStatus(String id) throws IOException {
        List<OrderDto> storage = ordersStorage.get(id);

        if (storage == null) {
            emulateOrders(id);
            return;
        }

        // check that storage active orders are still active in binance
        storage.removeIf(order -> !checkIfOrderIsActive(client.getOrder(order.getSymbol(), order.getOrderId())));

        // check if all trader's orders are copied, if not emulate
        List<PositionData> positions = activePositions(id).getData();
        positions.forEach(position -> {
            if (storage.stream().noneMatch(order -> order.getSymbol().equals(position.getSymbol()))) {
                double traderBalance = balance.get(id);
                if (traderBalance != 0) {
                    double budget = storage.size() <= 1 ? traderBalance / 3 : traderBalance * 0.5;
                    emulateOrder(id, position, budget);
                } else
                    log.info("Insufficient balance. Trader {} Order symbol {}", id, position.getSymbol());
            }
        });

        // check if trader executed one of orders then execute too
        storage.forEach(order -> {
            if (positions.stream().noneMatch(position -> position.getSymbol().equals(order.getSymbol()))) {
                executeOrder(id, order);
            }
        });
    }

    private static void executeOrder(String id, OrderDto orderDto) {
//        PositionDto positionDto = client.positionInfo().stream().filter(position -> position.getSymbol().equals(orderDto.getSymbol())).findFirst().get();


        OrderDto response = client.placeOrder(getMarketParams("APTUSDT", "SELL", String.valueOf(22)));
        System.out.println(new JSONObject(response).toString(2));
        System.out.println(client.positionInfo().size());
    }

    /**
     * Emulate all trader's active orders.
     * @param id trader id
     */
    @SneakyThrows
    private static void emulateOrders(String id) {
        double balance = CopyTradingApplication.balance.get(id);
        List<PositionData> activeOrders = activePositions(id).getData();
        int ordersSize = activeOrders.size();
        double partitionBalance =
                ordersSize <= 3 ? balance / 3 : balance / ordersSize;
        if (activeOrders.size() != 0) {
            for (PositionData position : activeOrders) {
                emulateOrder(id, position, partitionBalance);
            }
        }
    }

    /**
     * Emulate single order
     * @param id trader id
     * @param positionData position
     * @param budget price for this position
     */
    private static void emulateOrder(String id, PositionData positionData, double budget) {
        LinkedHashMap<String, Object> params = convertOrderParams(positionData, budget);
        OrderDto newOrder = client.placeOrder(params);
        List<OrderDto> updated = ordersStorage.get(id);
        updated.add(newOrder);
        ordersStorage.put(id, updated);
        double balanceUpdated = balance.get(id) - budget;
        balance.put(id, balanceUpdated);
        log.info("Emulated order. Trader id: {} Budget: {} Order: {}", id, budget, newOrder);
    }

}
