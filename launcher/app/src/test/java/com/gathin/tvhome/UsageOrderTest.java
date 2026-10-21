package com.gathin.tvhome;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UsageOrderTest {
    private static Map<String, Long> usage(Object... pairs) {
        Map<String, Long> m = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], ((Number) pairs[i + 1]).longValue());
        return m;
    }

    @Test public void mostUsedAppRanksFirst() {
        Map<String, Integer> r = UsageOrder.ranks(usage("netflix", 30_000, "disney", 20_000, "paramount", 6_000));
        assertEquals(0, (int) r.get("netflix"));
        assertEquals(1, (int) r.get("disney"));
        assertEquals(2, (int) r.get("paramount"));
    }

    @Test public void unusedAppsGetNoRank() {
        Map<String, Integer> r = UsageOrder.ranks(usage("netflix", 30_000, "max", 0));
        assertFalse(r.containsKey("max"));
        assertEquals(1, r.size());
    }

    @Test public void equalTimeFallsBackToPackageName() {
        Map<String, Integer> r = UsageOrder.ranks(usage("b.app", 5_000, "a.app", 5_000));
        assertEquals(0, (int) r.get("a.app"));
        assertEquals(1, (int) r.get("b.app"));
    }

    @Test public void noUsageDataGivesNoRanks() {
        assertTrue(UsageOrder.ranks(new HashMap<>()).isEmpty());
    }

    @Test public void rankedAppsSortBeforeUnrankedOnes() {
        Map<String, Integer> r = UsageOrder.ranks(usage("netflix", 30_000, "disney", 20_000));
        assertTrue(UsageOrder.compare(r, "netflix", "disney") < 0);
        assertTrue(UsageOrder.compare(r, "disney", "max") < 0);
        assertTrue(UsageOrder.compare(r, "max", "netflix") > 0);
        assertEquals(0, UsageOrder.compare(r, "max", "mercadoplay"));
    }
}
