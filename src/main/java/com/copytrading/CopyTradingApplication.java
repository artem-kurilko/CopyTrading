package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.copytradingleaderboard.model.request.FilterType;
import com.copytrading.copytradingleaderboard.model.request.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.model.BaseAsset;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.connector.config.BinanceConfig.testClient;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.*;
import static com.copytrading.model.BaseAsset.USDT;
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
        log.info("Selected traders {}", ids);
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
                }, 0, PARSE_POSITIONS_DELAY, TimeUnit.SECONDS);
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
                log.info("Initialized balance {}", initializedBalance);
                return initializedBalance;
            }
        }
        throw new RuntimeException("INIT BALANCE EXCEPTION. Traders ids: " + tradersIds + "\nBalance: " + client.balance());
    }

    @SneakyThrows
    public static void main(String[] args) {
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        balance = initBalance(ids, USDT);
        ordersStorage.put(ids.get(0), List.of(OrderDto.builder()
                        .symbol("BTCUSDT")
                        .orderId("")
                .build()));
        System.out.println(getLink(ids.get(0)));
        checkOrdersStatus(ids.get(0));
    }

    /**
     * Checks storage orders, trader leaderboard orders, if he has new then copy.
     * @param id traders leaderboard id
     * @throws IOException if exception occurs
     */
    private static void checkOrdersStatus(String id) throws IOException {
        List<OrderDto> storage = new ArrayList<>(ordersStorage.get(id));
        System.out.println(storage);

        if (storage.size() == 0) {
            emulateOrders(id);
            return;
        }

        // check that storage active orders are still active in binance, if not remove from storage
        storage.removeIf(storageOrder -> !checkIfPositionExists(storageOrder.getSymbol()));
        System.out.println("Storage after remove: " + storage);

        // check if all trader's orders are copied, if not emulate
        List<PositionData> positions = activePositions(id).getData();
        positions.forEach(position -> {
            if (storage.stream().noneMatch(storageOrder -> storageOrder.getSymbol().equals(position.getSymbol()))) {
                System.out.println("Emulate: ");
                //                emulateOrder(id, position);
            }
        });
        System.out.println("After o");
        // check if trader executed one of orders then execute too
        storage.forEach(storageOrder -> {
            if (positions.stream().noneMatch(position -> position.getSymbol().equals(storageOrder.getSymbol()))) {
                System.out.println("Execute");
                //                executeOrder(id, storageOrder);
            }
        });
        System.out.println("After a");
    }

    private static boolean checkIfPositionExists(String symbol) {
        return client.positionInfo().stream().anyMatch(position -> position.getSymbol().equals(symbol));
    }

    private static void executeOrder(String id, OrderDto orderDto) {
//        BalanceDto balanceDto = client.getCollateralBalanceOfSymbol(positionDto.getSymbol());
//        LinkedHashMap<String, Object> params = getMarketParams(positionDto.getSymbol(), getOppositeSide(positionDto), valueOf(positionDto.getPositionAmt()));
//        OrderDto response = client.placeOrder(params);
//        BalanceDto balanceDtoUpdate = client.getCollateralBalanceOfSymbol(positionDto.getSymbol());
//        double rpl = balanceDtoUpdate.getAvailableBalance() - balanceDto.getAvailableBalance();
//        balance.put(id, balance.get(id) + rpl);
//        log.info("Executed Symbol: {} RPL: {} Position {}", positionDto.getSymbol(), rpl, response);
    }

    /**
     * Emulate all trader's active orders.
     * @param id trader id
     */
    @SneakyThrows
    private static void emulateOrders(String id) {
        List<PositionData> activeOrders = activePositions(id).getData();
        if (activeOrders.size() != 0) {
            for (PositionData position : activeOrders) {
                emulateOrder(id, position);
            }
        }
    }

    /**
     * Emulate single order
     * @param id trader id
     * @param positionData position
     */
    private static void emulateOrder(String id, PositionData positionData) {
        double traderBalance = balance.get(id);
       /* if (traderBalance != 0) {
            double budget = storage.size() <= 1 ? traderBalance / 3 : traderBalance * 0.5;
            emulateOrder(id, position, budget);
        } else
            log.info("Insufficient balance. Trader {} Order symbol {}", id, position.getSymbol());
*/

    /*    LinkedHashMap<String, Object> params = convertOrderParams(positionData, budget);
        OrderDto newOrder = client.placeOrder(params);
        List<OrderDto> updated = ordersStorage.get(id);
        updated.add(newOrder);
        ordersStorage.put(id, updated);
        double balanceUpdated = balance.get(id) - budget;
        balance.put(id, balanceUpdated);*/
//        log.info("Emulated order. Trader id: {} Budget: {} Order: {}", id, budget, newOrder);
    }

}
