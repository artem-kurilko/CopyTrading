package com.copytrading;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.model.OrderSide;
import com.copytrading.service.LeadTraderDatabaseService;
import com.copytrading.sources.binance.futuresleaderboard.model.request.StatisticsType;
import com.copytrading.sources.binance.futuresleaderboard.model.response.position.Position;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.model.OrderSide.*;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static com.copytrading.service.OrderConverterService.round;
import static com.copytrading.sources.binance.futuresleaderboard.FuturesLeaderboardScrapper.*;
import static com.copytrading.sources.binance.futuresleaderboard.model.request.PeriodType.MONTHLY;

/**
 * Simple bot alternative.
 * This bot just iterate via copy traders orders and place position with fixed balance.
 *
 * @author Artemii Kurilko
 * @version 2.3
 */
public class SimplePositionNotifier {
    /**
     * Trading mode: testEnv=false, prodEnv=true
     */
    private static boolean mode = false;

    private static final BinanceConnector client = new BinanceConnector(mode);
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(mode);
    private static final ScheduledLeftOrdersProcessor leftOrdersProcessor = new ScheduledLeftOrdersProcessor(mode);
    private static final Logger log = initLogger();

    // trading
    private static HashMap<String, List<String>> leadTradersOrders = new HashMap<>();
    private static final HashMap<String, Integer> leverageStorage = new HashMap<>();
    private static int FIXED_MARGIN_PER_ORDER;
    private static final int maxNumberOfOrders = 20;
    private static final int maxProfitAllowed = 3;
    private static final int DEFAULT_LEVERAGE = 12;

    // sockets
    private static final int SOCKET_RETRY_COUNT = 10;
    private static final int delay = 10;

    @SneakyThrows
    public static void mafin(String[] args) {
        ArrayList<String> ids;
        if (db.getLeaderIdsAndOrders().isEmpty()) {
            ids = new ArrayList<>(Arrays.asList(
//                    "1FB04E31362DEED9CAA1C7EF8A771B8A",
                    "ACD6F840DE4A5C87C77FB7A49892BB35",
                    "F3D5DFEBBB2FDBC5891FD4663BCA556F",
                    "E921F42DCD4D9F6ECC0DFCE3BAB1D11A",
                    "3BAFAFCA68AB85929DF777C316F18C54"
            ));
        } else {
            HashMap<String, List<String>> idsAndOrdersMap = db.getLeaderIdsAndOrders();
            ids = new ArrayList<>(idsAndOrdersMap.keySet());
            leadTradersOrders = idsAndOrdersMap;
        }
        leftOrdersProcessor.proceedLeftOrders();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (int i = 1; i <= SOCKET_RETRY_COUNT; i++) {
                            try {
                                tradersCheck(ids);
                                proceedTradersPositions(ids);
                                System.out.println();
                                return;
                            } catch (SocketTimeoutException |
                                     UnknownHostException |
                                     BinanceConnectorException |
                                     JsonSyntaxException |
                                     SocketException ex) {
                                ex.printStackTrace();
                                Thread.sleep(30000);
                            }
                        }
                    } catch (Exception e) {
                        executorService.shutdown();
                        db.resetLeaderIdsAndOrders(leadTradersOrders);
                        throw new RuntimeException(e);
                    }
                }, 0, delay, TimeUnit.SECONDS);
    }

    @SneakyThrows
    public static void main(String[] args) {
        List<String> ids = new ArrayList<>(Arrays.asList(
//                    "1FB04E31362DEED9CAA1C7EF8A771B8A",
                "ACD6F840DE4A5C87C77FB7A49892BB35",
                "F3D5DFEBBB2FDBC5891FD4663BCA556F",
                "E921F42DCD4D9F6ECC0DFCE3BAB1D11A",
                "3BAFAFCA68AB85929DF777C316F18C54"
        ));
   }

    private static void proceedTradersPositions(List<String> tradersIds) throws IOException {
        // actualize db active order with current open positions
        List<PositionDto> activePositions = client.positionInfo();
        // взять лефт ордерс тоже блять, тип если у нового трейдера появился такой же как и в лефт, то лефт привязать к этому трейдеру
        List<String> ordersIds = leadTradersOrders.values().stream().flatMap(List::stream).toList();


        // add all traders positions to map
        // if there are positions which don't exist in map, check if it's left, if not execute immediately, left order should have separate logic asynchronously
        // than excluding the ones I have, emulate them in order
        // add one more exchange to emulate



        // add all traders positions to map
        Map<String, Position> traderPositionMap = new HashMap<>();
        for (String id : tradersIds) {
            List<Position> tradersPositions = getTraderPositions(id).getData().getOtherPositionRetList();
            if (!tradersPositions.isEmpty()) {
                tradersPositions.forEach(position -> traderPositionMap.put(position.getSymbol(), position));
            }
        }

        // check if there are positions to execute
        List<PositionDto> activePositions = client.positionInfo();
        for (PositionDto positionDto : activePositions) {
            if (traderPositionMap.keySet().stream().noneMatch(symbol -> symbol.equals(positionDto.getSymbol()))) {
                executeOrder(positionDto, "leadTraderId", "orderId");
            }
        }

        // recalculate fixed_margin_per_order with each iteration, to make it more flexible to changes in balance
        BalanceDto balanceDto = client.balance(USDT.name());
        double walletBalance = balanceDto.getBalance() + balanceDto.getCrossUnPnl();
        FIXED_MARGIN_PER_ORDER = (int) walletBalance / maxNumberOfOrders;

        double availableBalance = balanceDto.getAvailableBalance();
        if (availableBalance < FIXED_MARGIN_PER_ORDER) {
            return;
        }

        // check if trader has new orders than add to set to emulate
        Set<Position> positionsToEmulate = new HashSet<>();
        for (String symbol : traderPositionMap.keySet()) {
            if (activePositions.stream().noneMatch(pos -> pos.getSymbol().equals(symbol))) {
                positionsToEmulate.add(traderPositionMap.get(symbol));
            }
        }

        if (positionsToEmulate.isEmpty()) {
            return;
        }

        // if balance is limited then start with ones where entry price closer to mark price
        if (availableBalance < positionsToEmulate.size() * FIXED_MARGIN_PER_ORDER) {
            List<Position> sortedPositions = positionsToEmulate.stream().sorted((o1, o2) -> {
                double o1Upl = o1.getPnl();
                double o2Upl = o2.getPnl();
                if (o1Upl < 0 && o2Upl > 0) {
                    return 1;
                } else if (o2Upl < 0 && o1Upl > 0) {
                    return -1;
                } else if (o1Upl < 0 && o2Upl < 0) {
                    double o1Diff = Math.abs(1 - o1.getEntryPrice() / o1.getMarkPrice());
                    double o2Diff = Math.abs(1 - o2.getEntryPrice() / o2.getMarkPrice());
                    return Double.compare(o2Diff, o1Diff);
                } else {
                    double o1Diff = Math.abs(1 - o1.getEntryPrice() / o1.getMarkPrice());
                    double o2Diff = Math.abs(1 - o2.getEntryPrice() / o2.getMarkPrice());
                    return Double.compare(o1Diff, o2Diff);
                }
            }).toList();
            for (Position positionData : sortedPositions) {
                try {
                    double margin = availableBalance >= FIXED_MARGIN_PER_ORDER ? FIXED_MARGIN_PER_ORDER : availableBalance;
                    emulateOrder(positionData, margin);
                    availableBalance -= FIXED_MARGIN_PER_ORDER;
                } catch (Exception e) {
                    log.info("ERROR: " + e.getMessage() + " Symbol: " + positionData.getSymbol() + " Position: " + positionData);
                    throw e;
                }
            }
        } else {
            positionsToEmulate.forEach(pos -> emulateOrder(pos, FIXED_MARGIN_PER_ORDER));
        }
    }

    private static void emulateOrder(Position positionData, double margin) {
        if (isToLateToCopy(positionData)) {
            return;
        }
        String symbol = positionData.getSymbol();
        int leverage = adjustLeverage(positionData);

        // Calculate order amount
        double amount = margin * leverage / positionData.getMarkPrice();
        if (symbol.equals("ETHBTC")) {
            System.out.println("==============================================================");
            String errorMessage = "ERROR: Emulate order cannot find symbol: " + positionData.getSymbol();
            System.out.println(errorMessage);
            System.out.println("==============================================================");
            return;
        }
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getPositionSide(positionData).name(),
                amount
        );
        try {
            OrderDto response = client.placeOrder(params);
            log.info("Emulated order. Symbol: " + response.getSymbol() + " Margin: $" + round(FIXED_MARGIN_PER_ORDER, 2));
            // add order to storage
            String traderId = positionData.getTraderId();
            List<String> updatedOrders = leadTradersOrders.get(traderId);
            if (updatedOrders != null) {
                updatedOrders.add(response.getOrderId());
                leadTradersOrders.put(traderId, updatedOrders);
            } else {
                leadTradersOrders.put(traderId, List.of(response.getOrderId()));
            }
        } catch (BinanceClientException clientException) {
            if (clientException.getMessage().contains("Order's notional must be no smaller")) {
                System.out.println("==============================================================");
                System.out.println("Exception: Symbol: " + symbol + " " + clientException.getMessage());
                System.out.println("==============================================================");
            } else {
                throw clientException;
            }
        }
    }

    /**
     * Sets leverage value for cryptocurrency pair the same as lead trader.
     * If leverage is higher than {@link #DEFAULT_LEVERAGE} than set to default.
     * @param positionData position to emulate
     * @return leverage value
     */
    private static int adjustLeverage(Position positionData) {
        String symbol = positionData.getSymbol();
        Integer initialLeverage = leverageStorage.get(symbol);
        if (initialLeverage == null) {
            initialLeverage = client.getLeverage(symbol);
            leverageStorage.put(symbol, DEFAULT_LEVERAGE);
        }

        int positionLeverage = positionData.getLeverage();
        if (positionLeverage != initialLeverage) {
            if (initialLeverage == DEFAULT_LEVERAGE) {
                return DEFAULT_LEVERAGE;
            } else if (positionLeverage > DEFAULT_LEVERAGE) {
                client.setLeverage(symbol, DEFAULT_LEVERAGE);
                return DEFAULT_LEVERAGE;
            } else {
                client.setLeverage(symbol, positionLeverage);
                leverageStorage.put(symbol, positionLeverage);
                return positionLeverage;
            }
        } else {
            if (initialLeverage > DEFAULT_LEVERAGE) {
                client.setLeverage(symbol, DEFAULT_LEVERAGE);
                return DEFAULT_LEVERAGE;
            } else {
                return initialLeverage;
            }
        }
    }

    private static void executeOrder(PositionDto positionDto, String traderId, String orderId) {
        String symbol = positionDto.getSymbol();
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getOppositeSide(positionDto),
                Math.abs(positionDto.getPositionAmt())
        );
        OrderDto response = client.placeOrder(params);
        System.out.println(response);
        // remove order is from list
        if (leadTradersOrders.get(traderId) != null) {
            leadTradersOrders.get(traderId).remove(orderId);
        } else {
            leadTradersOrders.put(traderId, Collections.emptyList());
        }
        log.info("Executed Symbol: " + symbol + " UPL: " + positionDto.getUnRealizedProfit());
    }

    /**
     * Validation before emulating position.
     * Checks that if we copy late and trader received >= {@link #maxProfitAllowed} percentage of profit than don't emulate position.
     * @param positionData {@link Position} instance
     * @return boolean value
     */
    private static boolean isToLateToCopy(Position positionData) {
        OrderSide side = getPositionSide(positionData);
        double entry = positionData.getEntryPrice();
        double mark = positionData.getMarkPrice();
        if (side.equals(BUY) && ((mark * 100 / entry) - 100) <= maxProfitAllowed) {
            return false;
        }
        if (side.equals(SELL) && (mark * 100 / entry) >= (100-maxProfitAllowed)) {
            return false;
        }
        return true;
    }

    /**
     * Checks that trader is active and shares his positions,
     * If not replace with next in ranking trader.
     * @param tradersIds list of ids
     * @throws IOException if exception occurs
     */
    private static void tradersCheck(List<String> tradersIds) throws IOException {
        List<String> iterateIds = new ArrayList<>(tradersIds);
        for (String id : iterateIds) {
            if (!getTradersBaseInfo(id).getData().isPositionShared()) {
                // replace lead trader id
                String leadId = getNextTopTrader(tradersIds, MONTHLY, StatisticsType.PNL);
                tradersIds.remove(id);
                tradersIds.add(leadId);

                // transfer trader orders to left orders
                List<String> leftOrderList = leadTradersOrders.remove(id);
                leadTradersOrders.put(leadId, Collections.emptyList());
                if (leftOrderList != null && !leftOrderList.isEmpty()) {
                    db.saveLeftOrders(leftOrderList);
                }
            }
        }
    }

    @SneakyThrows
    private static Logger initLogger() {
        String fileName = mode ? "server_log.txt" : "test_log.txt";
        Logger logger = Logger.getLogger("CopyTradingBot");
        FileHandler fh = new FileHandler(fileName, true);
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
