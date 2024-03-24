package com.copytrading;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.exception.InsufficientMarginException;
import com.copytrading.model.OrderSide;
import com.copytrading.service.LeadTraderDatabaseService;
import com.copytrading.sources.futuresleaderboard.model.request.StatisticsType;
import com.copytrading.sources.futuresleaderboard.model.response.leaderboard.Leader;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

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
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.*;
import static com.copytrading.sources.futuresleaderboard.model.request.PeriodType.MONTHLY;

/**
 * Simple bot alternative.
 * This bot just iterate via copy traders orders and place position with fixed balance.
 *
 * @author Artemii Lepshokov
 * @version 2.4
 */
public class SimplePositionNotifier {
    /**
     * Trading mode: testEnv=false, prodEnv=true
     */
    private static final boolean mode = true;

    // common
    private static final BinanceConnector client = new BinanceConnector(mode);
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(mode);
    private static final AsyncUnmarkedOrdersProcessor leftOrdersProcessor = new AsyncUnmarkedOrdersProcessor(mode);
    private static final HashMap<String, Integer> leverageStorage = new HashMap<>();
    public static final Logger log = initLogger();

    // trading
    private static final int numOfLeadTraders = 5;
    private static final int maxNumberOfOrders = 15;
    // max price change in percentage, i.e. lead trader position long, and price raised to 3 percentage then don't copy
    private static final int maxProfitAllowed = 3;
    private static final int MAX_LEVERAGE = 12;

    // sockets
    private static final int SOCKET_RETRY_COUNT = 10;
    private static final int delay = 10;

    @SneakyThrows
    public static void main(String[] args) {
        // disable mongo info logs
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.ERROR);

        // actualize that mark and unmarked orders exist
        db.actualizeDB();
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
                                handleException(ex);
                                Thread.sleep(30000);
                            }
                        }
                    } catch (NullPointerException ex) {
                        if (ex.getMessage().contains("return value of \"com.copytrading.sources.binance.futuresleaderboard.model.response.position.TraderPositions.getData()\" is null")) {
                            log.info("EXCEPTION SHUTDOWN: Futures leaderboard positions cookies invalid.");
                        }
                        handleException(ex);
                    } catch (Exception e) {
                        handleException(e);
                    }
                }, 0, delay, TimeUnit.SECONDS);
    }

    private static List<String> getIds() throws IOException {
        List<String> ids = db.getLeaderIds();
        if (ids.isEmpty()) {
            ids = new ArrayList<>(validFuturesLeaderboard(MONTHLY, StatisticsType.PNL, numOfLeadTraders).stream().map(Leader::getEncryptedUid).toList());
            ids.forEach(db::saveNewTrader);
        }
        while (ids.size() < numOfLeadTraders) {
            String nextId = getNextTopTrader(ids, MONTHLY, StatisticsType.PNL);
            ids.add(nextId);
            db.saveNewTrader(nextId);
        }
        tradersCheck(ids);
        return ids;
    }

    private static void proceedTradersPositions(List<String> tradersIds) throws IOException {
        // add all traders positions to map
        Map<String, Position> traderPositionMap = new HashMap<>();
        for (String id : tradersIds) {
            List<Position> tradersPositions = getTraderPositions(id).getData().getOtherPositionRetList();
            if (!tradersPositions.isEmpty()) {
                for (Position position : tradersPositions) {
                    String positionSymbol = position.getSymbol();
                    traderPositionMap.put(positionSymbol, position);
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

        // recalculate fixed_margin_per_order with each iteration, to make it more flexible to changes in balance
        BalanceDto balanceDto = client.balance(USDT);
        double walletBalance = balanceDto.getBalance() + balanceDto.getCrossUnPnl();
        int FIXED_MARGIN_PER_ORDER = (int) walletBalance / maxNumberOfOrders;
        double availableBalance = balanceDto.getAvailableBalance();

        // if balance is limited then start with ones where entry price closer to mark price
        if (availableBalance < positionsToEmulate.size() * FIXED_MARGIN_PER_ORDER) {
            List<Position> sortedPositions = sortPositions(positionsToEmulate);
            for (Position positionData : sortedPositions) {
                double margin = availableBalance >= FIXED_MARGIN_PER_ORDER ? FIXED_MARGIN_PER_ORDER : availableBalance;
                emulateOrder(positionData, margin);
                availableBalance -= FIXED_MARGIN_PER_ORDER;
            }
        } else {
            positionsToEmulate.forEach(pos -> emulateOrder(pos, FIXED_MARGIN_PER_ORDER));
        }
    }

    /**
     * Algorithm to sort positions from most to less preferable.
     * The best position is where trader has negative pnl, so our profit is greater
     * If both positions have positive pnl choose where mark price is closer to entry price
     * @param positions set of positions
     * @return sorted list
     */
    public static List<Position> sortPositions(Set<Position> positions) {
        return positions.stream().sorted((o1, o2) -> {
            double o1Upl = o1.getPnl();
            double o2Upl = o2.getPnl();
            if (o1Upl < 0 && o2Upl > 0) {
                return -1;
            } else if (o2Upl < 0 && o1Upl > 0) {
                return 1;
            } else if (o1Upl < 0 && o2Upl < 0) {
                double o1Diff = Math.abs(1 - o1.getEntryPrice() / o1.getMarkPrice());
                double o2Diff = Math.abs(1 - o2.getEntryPrice() / o2.getMarkPrice());
                return Math.max(o1Diff, o2Diff) == o2Diff ? 1 : -1;
            } else {
                double o1Diff = Math.abs(1 - o1.getEntryPrice() / o1.getMarkPrice());
                double o2Diff = Math.abs(1 - o2.getEntryPrice() / o2.getMarkPrice());
                return Math.min(o1Diff, o2Diff) == o2Diff ? 1 : -1;
            }
        }).toList();
    }

    private static void emulateOrder(Position position, double margin) {
        if (isToLateToCopy(position)) {
            return;
        }
        String symbol = position.getSymbol();
        int leverage = adjustLeverage(position);
        double amount = margin * leverage / position.getMarkPrice();

        if (symbol.equals("ETHBTC")) {
            // double minNotional = 0.001;
            System.out.println("==== EXCEPTION: ETHBTC logic doesn't support");
            return;
        }

        try {
            LinkedHashMap<String, Object> params = getMarketOrderParams(
                    symbol,
                    getPositionSide(position).name(),
                    amount
            );
            OrderDto response = client.placeOrder(params);
            db.saveOrderToTrader(position.getTraderId(), symbol);
            log.info("Emulated order. Symbol: " + response.getSymbol() + " Margin: $" + round(margin, 2) + " Trader: " + position.getTraderId());
        } catch (InsufficientMarginException ex) {
            System.out.println("==== EXCEPTION: " + ex.getMessage());
        } catch (BinanceClientException clientException) {
            if (clientException.getMessage().contains("Order's notional must be no smaller")) {
                System.out.println("==== EXCEPTION: Symbol: " + symbol + " " + clientException.getMessage());
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
        String traderId = db.removeOrderFromTrader(symbol);
        log.info("Executed Symbol: " + symbol + " UPL: " + positionDto.getUnRealizedProfit() + " TraderId: " + traderId);
    }

    /**
     * Checks that trader is active and shares his positions,
     * If not replace with next in ranking trader.
     * @param tradersIds list of ids
     * @throws IOException if exception occurs
     * @return false if traders hide their positions and were updated, true if not
     */
    public static boolean tradersCheck(List<String> tradersIds) throws IOException {
        List<String> iterateIds = new ArrayList<>(tradersIds);
        boolean res = true;
        for (String id : iterateIds) {
            if (!isLeadTraderValid(id)) {
                res = false;
                // replace lead trader id
                String leadId = getNextTopTrader(tradersIds, MONTHLY, StatisticsType.PNL);
                tradersIds.remove(id);
                tradersIds.add(leadId);
                db.saveNewTrader(leadId);

                // transfer trader orders to unmarked orders
                List<String> unmarkedOrders = db.getAndRemoveTradersSymbols(id);
                if (!unmarkedOrders.isEmpty()) {
                    db.saveUnmarkedOrders(unmarkedOrders);
                }
            }
        } return res;
    }

    /**
     * Sets leverage value for cryptocurrency pair the same as lead trader.
     * If leverage is higher than {@link #MAX_LEVERAGE} than set to default.
     * @param positionData position to emulate
     * @return leverage value
     */
    private static int adjustLeverage(Position positionData) {
        String symbol = positionData.getSymbol();
        Integer initialLeverage = leverageStorage.get(symbol);
        if (initialLeverage == null) {
            initialLeverage = client.getLeverage(symbol);
            leverageStorage.put(symbol, MAX_LEVERAGE);
        }

        int positionLeverage = positionData.getLeverage();
        if (positionLeverage != initialLeverage) {
            if (initialLeverage == MAX_LEVERAGE) {
                return MAX_LEVERAGE;
            } else if (positionLeverage > MAX_LEVERAGE) {
                client.setLeverage(symbol, MAX_LEVERAGE);
                return MAX_LEVERAGE;
            } else {
                client.setLeverage(symbol, positionLeverage);
                leverageStorage.put(symbol, positionLeverage);
                return positionLeverage;
            }
        } else {
            if (initialLeverage > MAX_LEVERAGE) {
                client.setLeverage(symbol, MAX_LEVERAGE);
                return MAX_LEVERAGE;
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

    private static void handleException(Exception e) {
        e.printStackTrace();
    }

}
