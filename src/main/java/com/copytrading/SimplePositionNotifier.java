package com.copytrading;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.model.OrderSide;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.copytrading.CopyTradingApplication.log;
import static com.copytrading.connector.config.BinanceConfig.futuresClient;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.model.OrderSide.*;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static java.lang.Double.parseDouble;

/**
 * Simple bot alternative.
 * This bot just iterate via copy traders orders and place position with fixed balance.
 *
 * @author Artemii Kurilko
 * @version 1.1
 */
//TODO: - сделать суб аки, 90%-95% баланса проверенные трейдеры, остальное на тестирование других + другие параметры (roi, copy count)
public class SimplePositionNotifier {
    private static final BinanceConnector client = new BinanceConnector(futuresClient());
    private static final int FIXED_AMOUNT_PER_ORDER = 5; // margin per order (leverage not included)
    private static final int delay = 20;
    private static final int SOCKET_RETRY_COUNT = 3;
    private static final int maxProfitAllowed = 5; // max allowed profit (percentage) from lead trader position to emulate
    private static final int DEFAULT_LEVERAGE = 10;

    @SneakyThrows
    public static void main(String[] args) {
//        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);
        List<String> ids = List.of("3708884547500009217", "3753191121425897472", "3745161142246130945", "909110824361279489");

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (int i = 1; i <= SOCKET_RETRY_COUNT; i++) {
                            try {
                                proceedTradersPositions(ids);
                                System.out.println();
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
                }, 0, delay, TimeUnit.SECONDS);
    }

    private static void proceedTradersPositions(List<String> tradersIds) throws IOException {
        // add all traders positions to map
        Map<String, PositionData> traderPositionMap = new HashMap<>();
        for (String id : tradersIds) {
            List<PositionData> tradersPositions = activePositions(id).getData();
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
                executeOrder(positionDto.getSymbol());
            }
        }

        // if balance less than minimum than exit method
        BalanceDto balanceDto = client.balance(USDT.name());
        Double availableBalance = balanceDto.getAvailableBalance();
        if (availableBalance < FIXED_AMOUNT_PER_ORDER) {
            return;
        }

        // check if trader has new orders than add to set to emulate
        Set<PositionData> positionsToEmulate = new HashSet<>();
        for (String symbol : traderPositionMap.keySet()) {
            if (activePositions.stream().noneMatch(pos -> pos.getSymbol().equals(symbol))) {
                positionsToEmulate.add(traderPositionMap.get(symbol));
            }
        }

        if (positionsToEmulate.size() == 0) {
            return;
        }

        // if balance is limited then start with ones where entry price closer to mark price
        if (availableBalance < positionsToEmulate.size() * FIXED_AMOUNT_PER_ORDER) {
            List<PositionData> sortedPositions = positionsToEmulate.stream().sorted((o1, o2) -> {
                double o1Upl = parseDouble(o1.getUnrealizedProfit());
                double o2Upl = parseDouble(o2.getUnrealizedProfit());
                if (o1Upl < 0 && o2Upl > 0) {
                    return 1;
                } else if (o2Upl < 0 && o1Upl > 0) {
                    return -1;
                } else if (o1Upl < 0 && o2Upl < 0) {
                    double o1Diff = Math.abs(1 - parseDouble(o1.getEntryPrice()) / parseDouble(o1.getMarkPrice()));
                    double o2Diff = Math.abs(1 - parseDouble(o2.getEntryPrice()) / parseDouble(o2.getMarkPrice()));
                    return Double.compare(o2Diff, o1Diff);
                } else {
                    double o1Diff = Math.abs(1 - parseDouble(o1.getEntryPrice()) / parseDouble(o1.getMarkPrice()));
                    double o2Diff = Math.abs(1 - parseDouble(o2.getEntryPrice()) / parseDouble(o2.getMarkPrice()));
                    return Double.compare(o1Diff, o2Diff);
                }
            }).collect(Collectors.toList());
            for (PositionData positionData : sortedPositions) {
                try {
                    if (client.balance(USDT.name()).getAvailableBalance() >= FIXED_AMOUNT_PER_ORDER) {
                        emulateOrder(positionData);
                    }
                } catch (Exception e) {
                    log.info("ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            positionsToEmulate.forEach(SimplePositionNotifier::emulateOrder);
        }
    }

    private static void emulateOrder(PositionData positionData) {
        if (isToLateToCopy(positionData)) {
            return;
        }
        int leverage = adjustLeverage(positionData);
        double amount = FIXED_AMOUNT_PER_ORDER  * leverage / parseDouble(positionData.getMarkPrice());
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                positionData.getSymbol(),
                getPositionSide(positionData).name(),
                amount
        );
        try {
            OrderDto response = client.placeOrder(params);
            log.info("Emulated order. Order: " + response);
        } catch (BinanceClientException clientException) {
            if (clientException.getMessage().contains("Order's notional must be no smaller")) {
                log.info("Exception: " + clientException.getMessage());
            } else {
                throw clientException;
            }
        }
    }

    /**
     * Sets leverage value for cryptocurrency pair the same as lead trader.
     * If leverage is higher than {@link DEFAULT_LEVERAGE} than set to default.
     * @param positionData position to emulate
     * @return leverage value
     */
    private static int adjustLeverage(PositionData positionData) {
        int initialLeverage = client.getLeverage(positionData.getSymbol());
        int positionLeverage = positionData.getLeverage();
        if (positionLeverage != initialLeverage) {
            if (positionLeverage > DEFAULT_LEVERAGE) { // too high, reset to default value
                client.setLeverage(positionData.getSymbol(), DEFAULT_LEVERAGE);
                return DEFAULT_LEVERAGE;
            } else {
                client.setLeverage(positionData.getSymbol(), positionLeverage);
                return positionLeverage;
            }
        } else {
            return initialLeverage;
        }
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
     * Validation before emulating position, if position already exists then don't emulate,
     * Also checks that if we copy late and trader received >= 10% of profit than don't emulate position.
     * @param positionData {@link PositionData} instance
     * @return boolean value
     */
    private static boolean isToLateToCopy(PositionData positionData) {
        OrderSide side = getPositionSide(positionData);
        double entry = Double.parseDouble(positionData.getEntryPrice());
        double mark = Double.parseDouble(positionData.getMarkPrice());
        if (side.equals(BUY) && ((mark * 100 / entry) - 100) <= maxProfitAllowed) {
            return false;
        }
        if (side.equals(SELL) && (mark * 100 / entry) >= (100-maxProfitAllowed)) {
            return false;
        }
        return true;
    }

}
