package com.copytrading.service;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.connector.model.OrderDto;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.copytradingleaderboard.model.request.FilterType;
import com.copytrading.copytradingleaderboard.model.request.TimeRange;
import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.copytrading.CopyTradingApplication.log;
import static com.copytrading.connector.config.BinanceConfig.futuresClient;
import static com.copytrading.model.OrderSide.getOppositeSide;
import static com.copytrading.model.OrderSide.getPositionSide;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.copytradingleaderboard.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static com.copytrading.util.ConfigUtils.PARSE_POSITIONS_DELAY;
import static java.lang.Double.parseDouble;

/**
 * Simple bot alternative.
 * This bot just iterate via copy traders orders and place position with fixed balance.
 *
 * @author Artemii Kurilko
 * @version 1.0
 */
public class SimplePositionNotifier {
    private static final BinanceConnector client = new BinanceConnector(futuresClient());
    private static final int FIXED_AMOUNT_PER_ORDER = 4;

    @SneakyThrows
    public static void main(String[] args) {
        List<String> ids = getTradersIds(3, TimeRange.D30, FilterType.COPIER_PNL);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        proceedTradersPositions(ids);
                    } catch (Exception e) {
                        executorService.shutdown();
                        log.info("=================================================\n");
                        e.printStackTrace();
                    }
                }, 0, PARSE_POSITIONS_DELAY, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private static void proceedTradersPositions(List<String> tradersIds) {
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
        traderPositionMap.put("BTCUSDT", new PositionData());

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
                    emulateOrder(positionData);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("ERROR: " + e.getMessage());
                    return;
                }
            }
        } else {
            positionsToEmulate.forEach(SimplePositionNotifier::emulateOrder);
        }
    }

    private static void emulateOrder(PositionData positionData) {
        double amount = (double) FIXED_AMOUNT_PER_ORDER / parseDouble(positionData.getMarkPrice());
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                positionData.getSymbol(),
                getPositionSide(positionData).name(),
                amount
        );
        OrderDto response = client.placeOrder(params);
        log.info("Emulated order. Order: " + response);
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

}
