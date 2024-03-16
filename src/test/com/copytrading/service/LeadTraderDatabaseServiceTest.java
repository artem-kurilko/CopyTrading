package com.copytrading.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeadTraderDatabaseServiceTest {
    private static final LeadTraderDatabaseService db = new LeadTraderDatabaseService(false);

    @Test
    public void resetLeaderIdsAndOrdersTest() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        String uidOne = UUID.randomUUID().toString();
        String uidTwo = UUID.randomUUID().toString();
        String wordOne = "banana";
        String wordTwo = "apple";
        tradersIds.put(uidOne, List.of(wordOne, wordOne, wordOne));
        tradersIds.put(uidTwo, List.of(wordTwo, wordTwo, wordTwo));
        db.resetLeaderIdsAndOrders(tradersIds);

        HashMap<String, List<String>> res = db.getLeaderIdsAndOrders();
        assertEquals(2, res.size());
        assertEquals(tradersIds.get(uidOne), res.get(uidOne));
        assertEquals(tradersIds.get(uidTwo), res.get(uidTwo));
    }

    @Test
    public void resetLeftOrdersTest() {
        List<String> leftOrders = List.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        db.resetLeftOrders(leftOrders);

        List<String> res = db.getLeftOrders();
        assertEquals(4, res.size());
    }

}
