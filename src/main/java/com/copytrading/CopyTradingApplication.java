package com.copytrading;

import com.copytrading.leaderboard.copytrading.model.FilterType;
import com.copytrading.leaderboard.copytrading.model.TimeRange;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;
import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.getTradersIds;
import static com.copytrading.util.ConfigUtils.PARSE_POSITIONS_DELAY;

/**
 * Entry point of application.
 * @author Kurilko Artemii
 * @version 1.0.0
 */
public class CopyTradingApplication {

    public static void main(String[] args) throws IOException {
        System.out.println("Running... " + new Date());
        int partitions = 3;
        List<String> ids = getTradersIds(partitions, TimeRange.D30, FilterType.COPIER_PNL);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        for (String id : ids) {
                            ActivePositions positions = activePositions(id);
                            if (positions.getData().size()!=0) {
                                emulateOrders(positions.getData());
                            }
                        }
                    } catch (Exception e) {
                        executorService.shutdown();
                        e.printStackTrace();
                    }
                }, 0, PARSE_POSITIONS_DELAY, TimeUnit.MILLISECONDS);
    }

    private synchronized static void emulateOrders(List<PositionData> activeOrders) {
        for (PositionData order : activeOrders) {
            System.out.println("do some logic");
        }
    }

}
