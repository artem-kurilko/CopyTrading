package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.copytradingleaderboard.model.request.FilterType;
import com.copytrading.copytradingleaderboard.model.request.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.model.BaseAsset;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.copytrading.connector.config.BinanceConfig.testClient;
import static com.copytrading.connector.model.OrderSide.getOppositeSide;
import static com.copytrading.connector.model.OrderSide.getPositionSide;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.util.ConfigUtils.PARSE_POSITIONS_DELAY;
import static java.lang.Double.parseDouble;

/**
 * Entry point of application.
 * @author Kurilko Artemii
 * @version 1.0.0
 */
public class CopyTradingApplication {
    private static final BinanceConnector client = new BinanceConnector(testClient());
    private static final int partitions = 3; // amount of traders to follow and divide balance equally
    private static HashMap<String, Double> balance = new HashMap<>(); // available balance for copying each trader
    private static final HashMap<String, List<OrderDto>> ordersStorage = new HashMap<>(); // stores trader id and copied active orders

    private static final Logger log = initLogger();

    public static void main(String[] args) throws IOException {
        log.info("Started application... ");
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        log.info("Selected traders " + ids);
        balance = initBalance(ids, USDT);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (String id : ids)
                            checkOrdersStatus(id);
                    } catch (Exception e) {
                        executorService.shutdown();
                        log.info("=================================================\n");
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
                log.info("Initialized balance " + initializedBalance);
                return initializedBalance;
            }
        }
        throw new RuntimeException("INIT BALANCE EXCEPTION. Traders ids: " + tradersIds + "\nBalance: " + client.balance());
    }

    /**
     * Checks storage orders, trader leaderboard orders, if he has new then copy.
     * @param id traders leaderboard id
     * @throws IOException if exception occurs
     */
    private static void checkOrdersStatus(String id) throws IOException {
        if (ordersStorage.get(id) == null) {
            emulateOrders(id);
            return;
        }
        List<OrderDto> storage = new ArrayList<>(ordersStorage.get(id));

        // remove order from storage if there is no position with such symbol
        storage.removeIf(storageOrder -> !checkIfPositionExists(storageOrder.getSymbol()));

        // check if all trader's orders are copied, if not emulate
        List<PositionData> positions = activePositions(id).getData();
        positions.forEach(position -> {
            if (storage.stream().noneMatch(storageOrder -> storageOrder.getSymbol().equals(position.getSymbol()))) {
                emulateOrder(id, position);
            }
        });

        // check if trader executed one of orders then execute too
        storage.forEach(storageOrder -> {
            if (positions.stream().noneMatch(position -> position.getSymbol().equals(storageOrder.getSymbol()))) {
                executeOrder(id, storageOrder.getSymbol());
            }
        });
    }

    private static void executeOrder(String id, String symbol) {
        if (!checkIfPositionExists(symbol)) {
            throw new RuntimeException("Execute order, position doesn't exist " + symbol);
        }
        BalanceDto balanceDto = client.getCollateralBalanceOfSymbol(symbol);
        PositionDto positionDto = client.positionInfo(symbol);
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getOppositeSide(positionDto),
                Math.abs(positionDto.getPositionAmt())
        );
        OrderDto response = client.placeOrder(params);
        BalanceDto balanceDtoUpdate = client.getCollateralBalanceOfSymbol(symbol);
        double rpl = balanceDtoUpdate.getAvailableBalance() - balanceDto.getAvailableBalance();
        balance.put(id, balance.get(id) + rpl);
        List<OrderDto> updatedOrders = new LinkedList<>(ordersStorage.get(id));

        if (ordersStorage.get(id).size() != 0) {
            updatedOrders.removeIf(order -> order.getSymbol().equals(symbol));
            ordersStorage.put(id, updatedOrders);
        }

        if (checkIfPositionExists(symbol)) {
            throw new RuntimeException("ExecuteOrder position still exists ID: " + id + " Symbol: " + symbol);
        }

        log.info("Executed Symbol: " + positionDto.getSymbol() + " RPL: " + rpl + " Position " + response);
    }

    private static void executeOrder(String symbol) {
        PositionDto positionDto = client.positionInfo(symbol);
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getOppositeSide(positionDto),
                Math.abs(positionDto.getPositionAmt())
        );
        OrderDto response = client.placeOrder(params);
        log.info("Executed Symbol: " + positionDto.getSymbol() + " Position " + response);
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
        String symbol = positionData.getSymbol();
        List<OrderDto> storageOrders = ordersStorage.get(id);
        double availableBalance = balance.get(id);
        double budget;

        if (availableBalance == 0) {
            log.info("Insufficient balance. Trader " + id + " Position " + positionData);
            throw new IllegalArgumentException("Insufficient balance: " + availableBalance);
        }

        if (storageOrders != null) {
            budget = switch (storageOrders.size()) {
                case 0 -> availableBalance / 4;
                case 1 -> availableBalance / 3;
                case 2 -> availableBalance / 2;
                default -> availableBalance;
            };
        } else {
            budget = availableBalance / 4;
            ordersStorage.put(id, Collections.emptyList());
        }
        storageOrders = new LinkedList<>(ordersStorage.get(id));

        double amount = budget / parseDouble(positionData.getMarkPrice());
        BalanceDto balanceDto = client.getCollateralBalanceOfSymbol(symbol);
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getPositionSide(positionData).name(),
                amount
        );
        OrderDto response = client.placeOrder(params);
        storageOrders.add(response);
        ordersStorage.put(id, storageOrders);
        BalanceDto updatedBalanceDto = client.getCollateralBalanceOfSymbol(symbol);
        double rpl = updatedBalanceDto.getAvailableBalance() - balanceDto.getAvailableBalance();
        balance.put(id, balance.get(id) + rpl);
        log.info("Emulated order. Trader id: " + id + " Budget: " + budget + " Order: " + response);
    }

    private static boolean checkIfPositionExists(String symbol) {
        return client.positionInfo().stream().anyMatch(position -> position.getSymbol().equals(symbol));
    }

    /**
     * Checks that all active positions are in storage, if not then execute them as market.
     */
    private static void clearUnknownPositions() {
        Set<String> storageSymbols = new HashSet<>();
        for (String key : ordersStorage.keySet()) {
            List<OrderDto> positions = ordersStorage.get(key);
            positions.forEach(pos -> storageSymbols.add(pos.getSymbol()));
        }
        List<PositionDto> activePositions = client.positionInfo();
        for (PositionDto order : activePositions) {
            if (storageSymbols.stream().noneMatch(symbol -> symbol.equals(order.getSymbol()))) {
                executeOrder(order.getSymbol());
            }
        }
    }

    @SneakyThrows
    private static Logger initLogger() {
        Logger logger = Logger.getLogger("CopyTradingBot");
        FileHandler fh = new FileHandler("server_log.txt", true);
        fh.setFormatter(new Formatter() {
            @NotNull
            @Override
            public String format(@NotNull LogRecord record) {
                return String.format(new Date() + " "
                        + record.getLevel() + " " + record.getMessage() + "\n");
            }
        });
        logger.addHandler(fh);
        return logger;
    }

}
