package com.copytrading.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getLink;
import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.isPositionShared;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link LeadTraderDatabaseService}
 */
public class LeadTraderDatabaseServiceTest {
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(true);

    @Test
    public void dontRun() {
        List<String> ids = db.getLeaderIds();

        ids.forEach(x -> {
            try {
                System.out.println(x + " " + getLink(x));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void resetLeaderIdsAndOrdersTest() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        String uidOne = UUID.randomUUID().toString();
        String uidTwo = UUID.randomUUID().toString();
        List<String> fTraderSymbols = List.of("BTCUSDT", "ETHUSDT");
        List<String> sTraderSymbols = List.of("ETHBTC", "XRPUSDT");
        tradersIds.put(uidOne, fTraderSymbols);
        tradersIds.put(uidTwo, sTraderSymbols);
        db.resetLeaderIdsAndOrders(tradersIds);

        HashMap<String, List<String>> res = db.getLeaderIdsAndOrders();
        assertEquals(2, res.size());
        assertEquals(tradersIds.get(uidOne), res.get(uidOne));
        assertEquals(tradersIds.get(uidTwo), res.get(uidTwo));
    }

    @Test
    public void getAndRemoveTradersSymbolsTest() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        String uidOne = UUID.randomUUID().toString();
        String uidTwo = UUID.randomUUID().toString();
        List<String> fTraderSymbols = List.of("BTCUSDT", "ETHUSDT");
        List<String> sTraderSymbols = List.of("ETHBTC", "XRPUSDT");
        tradersIds.put(uidOne, fTraderSymbols);
        tradersIds.put(uidTwo, sTraderSymbols);
        db.resetLeaderIdsAndOrders(tradersIds);

        List<String> symbols = db.getAndRemoveTradersSymbols(uidOne);
        HashMap<String, List<String>> res = db.getLeaderIdsAndOrders();
        assertEquals(1, res.size());
        assertEquals(sTraderSymbols, res.get(uidTwo));
        assertEquals(2, symbols.size());
        assertTrue(symbols.containsAll(fTraderSymbols));
    }

    @Test
    public void saveOrderToTrader() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        String uid = UUID.randomUUID().toString();
        List<String> traderSymbols = List.of("BTCUSDT", "ETHUSDT");
        tradersIds.put(uid, traderSymbols);
        db.resetLeaderIdsAndOrders(tradersIds);
        db.saveOrderToTrader(uid, "BNBUSDT");

        HashMap<String, List<String>> res = db.getLeaderIdsAndOrders();
        assertEquals(1, res.size());
        assertEquals(3, res.get(uid).size());
        assertTrue(res.get(uid).containsAll(traderSymbols));
        assertTrue(res.get(uid).contains("BNBUSDT"));
    }

    @Test
    public void saveNewTraderTest() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        String uidOne = UUID.randomUUID().toString();
        String uidTwo = UUID.randomUUID().toString();
        List<String> fTraderSymbols = List.of("BTCUSDT", "ETHUSDT");
        List<String> sTraderSymbols = List.of("ETHBTC", "XRPUSDT");
        tradersIds.put(uidOne, fTraderSymbols);
        tradersIds.put(uidTwo, sTraderSymbols);
        db.resetLeaderIdsAndOrders(tradersIds);

        String newUid = UUID.randomUUID().toString();
        String newSymbol = "BNBUSDT";
        db.saveNewTrader(newUid);
        db.saveOrderToTrader(newUid, newSymbol);

        HashMap<String, List<String>> res = db.getLeaderIdsAndOrders();
        assertEquals(3, res.size());
        assertTrue(res.get(newUid).contains(newSymbol));
    }

    @Test
    public void removeOrderFromTraderTest() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        String uidOne = UUID.randomUUID().toString();
        String uidTwo = UUID.randomUUID().toString();
        List<String> fTraderSymbols = List.of("BTCUSDT", "ETHUSDT");
        List<String> sTraderSymbols = List.of("ETHBTC", "XRPUSDT");
        tradersIds.put(uidOne, fTraderSymbols);
        tradersIds.put(uidTwo, sTraderSymbols);
        db.resetLeaderIdsAndOrders(tradersIds);

        db.removeOrderFromTrader("ETHUSDT");
        HashMap<String, List<String>> res = db.getLeaderIdsAndOrders();
        assertEquals(2, res.size());
        assertEquals(1, res.get(uidOne).size());
        assertEquals("BTCUSDT", res.get(uidOne).get(0));
    }

    @Test
    public void resetUnmarkedOrdersTest() {
        List<String> unmarkedOrders = List.of(
                "BTCUSDT",
                "XRPUSDT",
                "DOGEUSDT",
                "BNBUSDT"
        );
        db.resetUnmarkedOrders(unmarkedOrders);
        List<String> res = db.getUnmarkedOrders();
        assertEquals(4, res.size());
    }

    @Test
    public void saveUnmarkedOrdersTest() {
        List<String> unmarkedOrders = List.of(
                "STRKUSDT",
                "XRPUSDT",
                "DOGEUSDT",
                "BNBUSDT"
        );
        db.resetUnmarkedOrders(unmarkedOrders);
        List<String> newSymbols = List.of("JUPUSDT", "PYTHUSDT");
        db.saveUnmarkedOrders(newSymbols);

        List<String> res = db.getUnmarkedOrders();
        assertEquals(6, res.size());
        assertTrue(res.containsAll(unmarkedOrders));
        assertTrue(res.containsAll(newSymbols));
    }

    @Test
    public void removeOrderFromUnmarkedOrdersTest() {
        List<String> unmarkedOrders = List.of(
                "STRKUSDT",
                "XRPUSDT",
                "DOGEUSDT",
                "BNBUSDT"
        );
        db.resetUnmarkedOrders(unmarkedOrders);
        db.removeOrderFromUnmarkedOrders("BNBUSDT");

        List<String> res = db.getUnmarkedOrders();
        assertEquals(3, res.size());
        assertFalse(res.contains("BNBUSDT"));
        assertFalse(res.containsAll(unmarkedOrders));
    }

}
