package com.copytrading;

import com.copytrading.connector.BinanceConnector;
import com.copytrading.connector.model.BalanceDto;
import com.copytrading.model.OrderSide;
import com.copytrading.service.LeadTraderDatabaseService;
import com.copytrading.sources.futuresleaderboard.model.request.StatisticsType;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static com.copytrading.SimplePositionNotifier.sortPositions;
import static com.copytrading.SimplePositionNotifier.tradersCheck;
import static com.copytrading.model.BaseAsset.USDT;
import static com.copytrading.model.OrderSide.getPositionSide;
import static com.copytrading.service.OrderConverterService.getMarketOrderParams;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getNextTopTrader;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getTradersBaseInfo;
import static com.copytrading.sources.futuresleaderboard.model.request.PeriodType.MONTHLY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class main bot service.
 * @see SimplePositionNotifier
 */
public class SimplePositionNotifierTest {
    private static final boolean mode = false;
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(mode);
    public static final BinanceConnector conn = new BinanceConnector(false);

    @Test
    public void testSome() {
        BalanceDto balanceDto = conn.balance(USDT);
        double walletBalance = balanceDto.getBalance() + balanceDto.getCrossUnPnl();
        int margin = (int) walletBalance / 15;

        String symbol = "SOLUSDT";
        int leverage = 4;
        double amount = margin * leverage / 203.3;

        LinkedHashMap<String, Object> params = getMarketOrderParams(
                symbol,
                OrderSide.BUY.name(),
                amount
        );
        System.out.println(params);
    }

    @Test
    public void getIdsDoesntExistTest() throws IOException {
        db.clearAllTraders();
        int numOfLeadTraders = 4;
        List<String> ids = db.getLeaderIds();
        List<String> invalidIds = List.of("B488164865E9B70D785A32CE8DCD5BC8", "FDA88B2379822A377FBB33F1DD392898");
        if (ids.isEmpty()) {
            ids = new ArrayList<>(List.of(
                    "ACD6F840DE4A5C87C77FB7A49892BB35"
            ));
            ids.addAll(invalidIds);
            ids.forEach(db::saveNewTrader);
        }

        String validId = "";
        while (ids.size() < numOfLeadTraders) {
            validId = getNextTopTrader(ids, MONTHLY, StatisticsType.PNL);
            ids.add(validId);
            db.saveNewTrader(validId);
        }
        tradersCheck(ids);

        List<String> dbIds = db.getLeaderIds();

        assertEquals(numOfLeadTraders, dbIds.size());
        assertTrue(dbIds.contains(validId));
        assertFalse(dbIds.containsAll(invalidIds));
    }

    @Test
    public void getIdsAlreadyExistTest() {
        HashMap<String, List<String>> traders = new HashMap<>();
        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();
        String uid3 = UUID.randomUUID().toString();
        traders.put(uid1, Collections.emptyList());
        traders.put(uid2, Collections.emptyList());
        traders.put(uid3, Collections.emptyList());
        db.resetLeaderIdsAndOrders(traders);

        List<String> ids = db.getLeaderIds();
        assertFalse(ids.isEmpty());
        assertEquals(3, ids.size());
        assertTrue(ids.contains(uid1));
        assertTrue(ids.contains(uid2));
        assertTrue(ids.contains(uid3));
    }

    @Test
    public void tradersCheckTest() throws IOException {
        List<String> tradersIds = new ArrayList<>(Arrays.asList(
                "1FB04E31362DEED9CAA1C7EF8A771B8A",
                "ACD6F840DE4A5C87C77FB7A49892BB35",
                "F3D5DFEBBB2FDBC5891FD4663BCA556F",
                "E921F42DCD4D9F6ECC0DFCE3BAB1D11A",
                "3BAFAFCA68AB85929DF777C316F18C54"
        ));
        db.clearAllTraders();
        db.clearUnmarkedOrders();

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
        }
        assertFalse(res);
    }

    @Test
    public void sortPositionsTest() {
        Set<Position> positions = new HashSet<>();
        positions.add(Position.builder().pnl(-1000).entryPrice(0.9990).markPrice(0.9990).build()); // 2
        positions.add(Position.builder().pnl(11012).entryPrice(9000).markPrice(13000).build()); // 3
        positions.add(Position.builder().pnl(-7428).entryPrice(500).markPrice(700).build()); // 1
        positions.add(Position.builder().pnl(10).entryPrice(10300).markPrice(2000).build()); // 4
        List<Position> sorted = sortPositions(positions);

        assertEquals(4, sorted.size());
        assertEquals(-7428, sorted.get(0).getPnl());
        assertEquals(-1000, sorted.get(1).getPnl());
        assertEquals(11012, sorted.get(2).getPnl());
        assertEquals(10, sorted.get(3).getPnl());
    }

//    @Test
    public void proceedTradersPositionsTest() {
//        db.clearAllTraders();
//        db.clearUnmarkedOrders();
    }

}
