package com.copytrading;

import com.copytrading.leaderboard.copytrading.model.response.positions.active.ActivePositions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.copytrading.leaderboard.copytrading.CopyLeaderboardScrapper.activePositions;
import static com.copytrading.util.ConfigUtils.PARSE_POSITIONS_DELAY;

/**
 * Entry point of application.
 * @author Kurilko Artemii
 * @version 1.0.0
 */
//TODO:
// - логировать ордера
// -
// - сначала сделать эмулятор ордеров
// - подключить тестнет и проверить (если не работает посмотреть тестнет хитбтс или сделать свой нахой)
// -
// - найти доверенные ай ди позиции которых будут парситься с задержкой
// - эти ордера сразу эмулировать в байненс тестнет, либо локально в бд например и логировать в csv
// - сделать скхедулер который будет парсить топ трейдеров и сохранять в логи, чтобы проверить кто из них харош
public class CopyTradingApplication {

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        String id = "3699966474805097216";
                        ActivePositions positions = activePositions(id);
                        System.out.println("Size: " + positions.getData().size());
                    } catch (Exception e) {
                        executorService.shutdown();
                        e.printStackTrace();
                    }
                }, 0, PARSE_POSITIONS_DELAY, TimeUnit.MILLISECONDS);
    }
}
