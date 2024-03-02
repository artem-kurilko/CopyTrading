package com.copytrading;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.futuresleaderboard.model.response.position.Position;
import com.copytrading.model.OrderSide;
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
import java.util.stream.Collectors;

import static com.copytrading.connector.config.BinanceConfig.futuresClient;
import static com.copytrading.futuresleaderboard.FuturesLeaderboardScrapper.getTraderPositions;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.model.OrderSide.*;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static com.copytrading.service.OrderConverterService.round;

/**
 * Simple bot alternative.
 * This bot just iterate via copy traders orders and place position with fixed balance.
 *
 * @author Artemii Kurilko
 * @version 2.0
 */

//TODO:
// - добавить байбит тестнет
public class SimplePositionNotifier {
    private static final BinanceConnector client = new BinanceConnector(futuresClient());
    private static final Logger log = initLogger();

    // trade
    private static final HashMap<String, Integer> leverageStorage = new HashMap<>();
    private static int FIXED_MARGIN_PER_ORDER;
    private static final int maxNumberOfOrders = 10;
    private static final int maxProfitAllowed = 3;
    private static final int DEFAULT_LEVERAGE = 10;

    // sockets
    private static final int SOCKET_RETRY_COUNT = 3;
    private static final int delay = 10;

    @SneakyThrows
    public static void main(String[] args) {
        List<String> ids = List.of(
                "1FB04E31362DEED9CAA1C7EF8A771B8A",
                "ACD6F840DE4A5C87C77FB7A49892BB35",
                "E4C2BCB6FDF2A2A7A20D516B8389B952"
        );
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
                                     SocketException ex) {
                                ex.printStackTrace();
                                Thread.sleep(30000);
                            }
                        }
                    } catch (Exception e) {
                        executorService.shutdown();
                        e.printStackTrace();
                    }
                }, 0, delay, TimeUnit.SECONDS);
    }

    private static void proceedTradersPositions(List<String> tradersIds) throws IOException {
        // add all traders positions to map
        Map<String, Position> traderPositionMap = new HashMap<>();
        for (String id : tradersIds) {
            List<Position> tradersPositions = getTraderPositions(id).getData().getOtherPositionRetList();
            if (tradersPositions.size() != 0) {
                tradersPositions.forEach(position -> {
                    traderPositionMap.put(position.getSymbol(), position);
                });
            }
        }

        // check if there are positions to execute
        List<PositionDto> activePositions = client.positionInfo();
        for (PositionDto positionDto : activePositions) {
            if (traderPositionMap.keySet().stream().noneMatch(symbol -> symbol.equals(positionDto.getSymbol()))) {
                executeOrder(positionDto);
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

        if (positionsToEmulate.size() == 0) {
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
            }).collect(Collectors.toList());
            for (Position positionData : sortedPositions) {
                try {
                    if (availableBalance >= FIXED_MARGIN_PER_ORDER) {
                        emulateOrder(positionData);
                    }
                    availableBalance -= FIXED_MARGIN_PER_ORDER;
                } catch (Exception e) {
                    log.info("ERROR: " + e.getMessage() + " Symbol: " + positionData.getSymbol() + " Position: " + positionData);
                    throw e;
                }
            }
        } else {
            positionsToEmulate.forEach(SimplePositionNotifier::emulateOrder);
        }
    }

    //FIXME:
    // - ETHBTC emulate order calculate amount
    private static void emulateOrder(Position positionData) {
        if (isToLateToCopy(positionData)) {
            return;
        }
        String symbol = positionData.getSymbol();
        int leverage = adjustLeverage(positionData);

        // Calculate order amount
        double amount = FIXED_MARGIN_PER_ORDER * leverage / positionData.getMarkPrice();
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
            log.info("Emulated order. Symbol: " + response.getSymbol() + " Amount: " + round(amount, 2));
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

    private static void executeOrder(PositionDto positionDto) {
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                positionDto.getSymbol(),
                getOppositeSide(positionDto),
                Math.abs(positionDto.getPositionAmt())
        );
        OrderDto response = client.placeOrder(params);
        log.info("Executed Symbol: " + positionDto.getSymbol() + " UPL: " + positionDto.getUnRealizedProfit() + " Position " + response);
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
