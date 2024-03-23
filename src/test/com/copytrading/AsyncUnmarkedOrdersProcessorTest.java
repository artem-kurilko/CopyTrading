package com.copytrading;

import com.copytrading.service.LeadTraderDatabaseService;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test class for async unmarked order's processor.
 * @see AsyncUnmarkedOrdersProcessor
 */
public class AsyncUnmarkedOrdersProcessorTest {
    private static final boolean mode = false;
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(mode);
    private static final AsyncUnmarkedOrdersProcessor leftOrdersProcessor = new AsyncUnmarkedOrdersProcessor(mode);

    @Test
    public void getMainOrderSideTest() {
        List<Position> positions = new LinkedList<>();
        positions.add(Position.builder().entryPrice(90).markPrice(300).pnl(-400).build());
        positions.add(Position.builder().entryPrice(100).markPrice(180).pnl(5).build());
        positions.add(Position.builder().entryPrice(120).markPrice(140).pnl(4).build());
        positions.add(Position.builder().entryPrice(100).markPrice(15).pnl(20).build());
    }

    @Test
    public void proceedLeftOrdersTest() throws InterruptedException {
        db.clearUnmarkedOrders();
        db.clearAllTraders();

        List<String> symbols = List.of("BTCUSDT", "BNBUSDT", "XRPUSDT", "CIAKAKAO");
        db.resetUnmarkedOrders(symbols);
        leftOrdersProcessor.proceedLeftOrders();
        Thread.sleep(60000);

        List<String> dbUnmarked = db.getUnmarkedOrders();
        List<String> dbTraders = db.getLeaderIds();

        System.out.println(dbUnmarked);
        System.out.println(dbTraders);

        assertFalse(dbUnmarked.isEmpty());
        assertFalse(dbTraders.isEmpty());
    }
}
