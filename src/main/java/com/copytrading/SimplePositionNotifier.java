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
 * @version 2.4
 */
public class SimplePositionNotifier {
    /**
     * Trading mode: testEnv=false, prodEnv=true
     */
    private static final boolean mode = false;

    private static final BinanceConnector client = new BinanceConnector(mode);
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(mode);
    private static final AsyncUnmarkedOrdersProcessor leftOrdersProcessor = new AsyncUnmarkedOrdersProcessor(mode);
    private static final Logger log = initLogger();

    // trading
    private static final HashMap<String, Integer> leverageStorage = new HashMap<>();
    private static int FIXED_MARGIN_PER_ORDER;
    private static final int maxNumberOfOrders = 20;
    private static final int maxProfitAllowed = 3;
    private static final int DEFAULT_LEVERAGE = 12;

    // sockets
    private static final int SOCKET_RETRY_COUNT = 10;
    private static final int delay = 10;

    @SneakyThrows
    public static void main(String[] args) {
        leftOrdersProcessor.proceedLeftOrders();
        List<String> ids = getIds();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (int i = 1; i <= SOCKET_RETRY_COUNT; i++) {
                            try {
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
                        throw new RuntimeException(e);
                    }
                }, 0, delay, TimeUnit.SECONDS);
    }

    private static List<String> getIds() {
        List<String> ids = db.getLeaderIds();
        if (ids.isEmpty()) {
            ids = new ArrayList<>(Arrays.asList(
//                    "1FB04E31362DEED9CAA1C7EF8A771B8A",
                    "ACD6F840DE4A5C87C77FB7A49892BB35",
                    "F3D5DFEBBB2FDBC5891FD4663BCA556F",
                    "E921F42DCD4D9F6ECC0DFCE3BAB1D11A",
                    "3BAFAFCA68AB85929DF777C316F18C54"
            ));
            ids.forEach(id -> db.saveNewTrader(id, Collections.emptyList()));
        }
        return ids;
    }

    private static void proceedTradersPositions(List<String> tradersIds) throws IOException {
        // add all traders positions to map
        List<String> unmarkedOrders = db.getUnmarkedOrders();
        Map<String, Position> traderPositionMap = new HashMap<>();
        for (String id : tradersIds) {
            List<Position> tradersPositions = getTraderPositions(id).getData().getOtherPositionRetList();
            if (!tradersPositions.isEmpty()) {
                for (Position position : tradersPositions) {
                    String positionSymbol = position.getSymbol();
                    traderPositionMap.put(positionSymbol, position);
                    // update unmarked orders
                    if (unmarkedOrders.stream().anyMatch(order -> order.equals(positionSymbol))) {
                        db.removeOrderFromUnmarkedOrders(positionSymbol);
                        db.saveNewTrader(position.getTraderId(), Collections.singletonList(positionSymbol));
                    }
                }
            }
        }
        
        // check if any of the traders hide their positions
        if (!tradersCheck(tradersIds)){
            return;
        }
        
        // check if there are positions to execute
        List<PositionDto> activePositions = client.positionInfo();
        for (PositionDto positionDto : activePositions) {
            if (traderPositionMap.keySet().stream().noneMatch(symbol -> symbol.equals(positionDto.getSymbol()))) {
                executeOrder(positionDto);
            }
        }

        // recalculate fixed_margin_per_order with each iteration, to make it more flexible to changes in balance
        BalanceDto balanceDto = client.balance(USDT);
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

    private static void emulateOrder(Position position, double margin) {
        if (isToLateToCopy(position)) {
            return;
        }
        String symbol = position.getSymbol();
        int leverage = adjustLeverage(position);

        // Calculate order amount
        double amount = margin * leverage / position.getMarkPrice();
        if (symbol.equals("ETHBTC")) {
            double minNotional = 0.001;
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            return;
        }
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getPositionSide(position).name(),
                amount
        );
        try {
            OrderDto response = client.placeOrder(params);
            db.saveOrderToTrader(position.getTraderId(), symbol);
            log.info("Emulated order. Symbol: " + response.getSymbol() + " Margin: $" + round(FIXED_MARGIN_PER_ORDER, 2));
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

    private static void executeOrder(PositionDto positionDto) {
        String symbol = positionDto.getSymbol();
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getOppositeSide(positionDto),
                Math.abs(positionDto.getPositionAmt())
        );
        client.placeOrder(params);
        db.removeOrderFromTrader(symbol);
        log.info("Executed Symbol: " + symbol + " UPL: " + positionDto.getUnRealizedProfit());
    }

    /**
     * Checks that trader is active and shares his positions,
     * If not replace with next in ranking trader.
     * @param tradersIds list of ids
     * @throws IOException if exception occurs
     * @return false if traders hide their positions and were updated, true if not
     */
    private static boolean tradersCheck(List<String> tradersIds) throws IOException {
        List<String> iterateIds = new ArrayList<>(tradersIds);
        boolean res = true;
        for (String id : iterateIds) {
            if (!getTradersBaseInfo(id).getData().isPositionShared()) {
                res = false;
                // replace lead trader id
                String leadId = getNextTopTrader(tradersIds, MONTHLY, StatisticsType.PNL);
                tradersIds.remove(id);
                tradersIds.add(leadId);
                // transfer trader orders to unmarked orders
                List<String> unmarkedOrders = db.getAndRemoveTradersSymbols(id);
                db.saveUnmarkedOrders(unmarkedOrders);
            }
        } return res;
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
