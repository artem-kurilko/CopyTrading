package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.PositionDto;
import com.copytrading.model.OrderSide;
import com.copytrading.service.LeadTraderDatabaseService;
import com.copytrading.sources.futuresleaderboard.model.request.PeriodType;
import com.copytrading.sources.futuresleaderboard.model.request.StatisticsType;
import com.copytrading.sources.futuresleaderboard.model.response.leaderboard.Leader;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.SimplePositionNotifier.log;
import static com.copytrading.model.OrderSide.*;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getTraderPositions;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.validFuturesLeaderboard;

/**
 * It's async class to proceed left orders, when lead trader doesn't show his positions no more, but we already copied them.
 * @see SimplePositionNotifier
 */
public class AsyncUnmarkedOrdersProcessor {
    // map to copy lead trader position, stores symbol as key and trader id as value
    private static final HashMap<String, String> copyTraderMap = new HashMap<>();
    private final int topTradersNum = 20;
    private final BinanceConnector client;
    private final LeadTraderDatabaseService db;

    private static final int delay = 5;
    private static final int waitNewUnmarkedOrdersDelayMillis = 30000;

    public AsyncUnmarkedOrdersProcessor(boolean isProd) {
        client = new BinanceConnector(isProd);
        db = new LeadTraderDatabaseService(isProd);
    }

    /**
     * Logic:
     * - if top {@link AsyncUnmarkedOrdersProcessor#topTradersNum} traders with open positions have these symbols than mark to them
     * - if not and positions have positive pnl, then execute now
     * - if not and positions have negative pnl, but other trader's positions have the same side, wait until pnl gets better
     * - if not and positions have negative pnl, and other trader's positions have other side than execute on negative pnl :(
     */
    public void proceedLeftOrders() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                // if there are no left orders then wait
                List<String> unmarkedOrders =  db.getUnmarkedOrders();
                if (unmarkedOrders.isEmpty()) {
                    Thread.sleep(waitNewUnmarkedOrdersDelayMillis);
                    return;
                }

                // top traders, stores trader id as key and his position list as value
                HashMap<String, List<Position>> leadTradersPositions = new HashMap<>();

                // check if top traders have same position, if yes assign unmarked order to them
                List<String> topTraderIds = validFuturesLeaderboard(PeriodType.MONTHLY, StatisticsType.PNL, topTradersNum)
                        .stream().map(Leader::getEncryptedUid).toList();
                for (String leaderId : topTraderIds) {
                    List<Position> positions = getTraderPositions(leaderId).getData().getOtherPositionRetList();
                    leadTradersPositions.put(leaderId, positions);
                    for (Position position : positions) {
                        String positionSymbol = position.getSymbol();
                        if (unmarkedOrders.stream().anyMatch(order -> order.equals(positionSymbol))) {
                            if (db.getLeaderIds().contains(leaderId)) {
                                unmarkedOrders.remove(positionSymbol);
                                db.removeOrderFromUnmarkedOrders(positionSymbol);
                                db.saveNewTrader(leaderId);
                                db.saveOrderToTrader(leaderId, positionSymbol);
                            } else {
                                copyTraderMap.putIfAbsent(positionSymbol, leaderId);
                            }
                        }
                    }
                }

                // execute unmarked orders if trader did
                if (!copyTraderMap.isEmpty()) {
                    copyTraderMap.forEach((symbol, traderId) -> {
                        if (leadTradersPositions.get(traderId).stream().noneMatch(pos -> pos.getSymbol().equals(symbol))) {
                            executeOrder(symbol, traderId);
                        }
                    });
                }

                // get unmarked orders without similar traders positions
                unmarkedOrders.removeAll(copyTraderMap.keySet());
                HashMap<String, PositionDto> currentPositions = new HashMap<>();
                client.positionInfo().forEach(pos -> currentPositions.put(pos.getSymbol(), pos));

                unmarkedOrders.forEach((symbol) -> {
                    PositionDto position = currentPositions.get(symbol);
                    if (position.getUnRealizedProfit() >= 0) {
                        executeOrder(symbol, null);
                    } else if (position.getUnRealizedProfit() < 0) {
                        // if other trader's positions have the same side, wait until pnl gets better, if not execute on negative pnl :(
                        OrderSide mainSide = getMainOrderSide(5);
                        OrderSide currentSide = getPositionSide(position);
                        if (!mainSide.equals(currentSide)) {
                            executeOrder(symbol, null);
                        }
                    }
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            },0, delay, TimeUnit.SECONDS);
    }

    private void executeOrder(String symbol, String traderId) {
        PositionDto positionDto = client.positionInfo(symbol);
        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                getOppositeSide(positionDto),
                Math.abs(positionDto.getPositionAmt())
        );
        client.placeOrder(params);
        copyTraderMap.remove(symbol);
        db.removeOrderFromUnmarkedOrders(symbol);
        log.info("Executed Unmarked Symbol: " + symbol + " UPL: " + positionDto.getUnRealizedProfit() + " Trader: " + traderId);
    }
}
