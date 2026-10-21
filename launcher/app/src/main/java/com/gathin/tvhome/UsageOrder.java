package com.gathin.tvhome;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tile order by how much each app was watched (config.xml sort_by_usage): the most used app first.
 *
 * Reads foreground time from UsageStatsManager over the last {@link #DAYS} days. That needs the
 * GET_USAGE_STATS app-op, which build-and-install grants with appops; without it the query returns nothing
 * and the tiles keep the usual rule (dock_apps, cards, alphabetical). Apps never used get no rank and follow
 * the ranked ones by that same rule.
 */
final class UsageOrder {

    private static final String TAG = "TvHome.UsageOrder";
    static final int DAYS = 30;

    private UsageOrder() {}

    /** Foreground time per package over the last DAYS days; empty when usage access is not granted. */
    static Map<String, Long> load(Context ctx) {
        Map<String, Long> out = new HashMap<>();
        UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return out;
        long now = System.currentTimeMillis();
        try {
            Map<String, UsageStats> stats = usm.queryAndAggregateUsageStats(now - DAYS * 86_400_000L, now);
            if (stats == null) return out;
            for (Map.Entry<String, UsageStats> e : stats.entrySet()) {
                out.put(e.getKey(), e.getValue().getTotalTimeInForeground());
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "usage stats unavailable", e);
        }
        return out;
    }

    /** Rank of each used package (0 = most used); ties by package name so the order is stable. */
    static Map<String, Integer> ranks(Map<String, Long> foregroundMs) {
        List<Map.Entry<String, Long>> used = new ArrayList<>();
        for (Map.Entry<String, Long> e : foregroundMs.entrySet()) {
            if (e.getValue() != null && e.getValue() > 0) used.add(e);
        }
        Collections.sort(used, (a, b) -> {
            int byTime = Long.compare(b.getValue(), a.getValue());
            return byTime != 0 ? byTime : a.getKey().compareTo(b.getKey());
        });
        Map<String, Integer> r = new HashMap<>();
        for (int i = 0; i < used.size(); i++) r.put(used.get(i).getKey(), i);
        return r;
    }

    /** Sort step for two packages: ranked ones first, most used first, then 0 to fall through. */
    static int compare(Map<String, Integer> ranks, String pkgA, String pkgB) {
        return Order.compare(ranks, pkgA, pkgB);
    }
}
