package com.copytrading;

import com.copytrading.model.PositionSide;
import com.copytrading.model.TradeDto;
import com.copytrading.model.TradeDtoData;
import com.copytrading.service.TradeDtoService;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SpringBootApplication
public class TestnetPaperTrading {
    private static final Logger log = initLogger();
    private static List<TradeDto> traders;
    private static final int delay = 20;

    public static void main(String[] args) {
        SpringApplication.run(TestnetPaperTrading.class);

        /*initTradersPositions();
        List<String> ids = new LinkedList<>(traders.keySet());

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService
                .scheduleWithFixedDelay(() -> {
                    try {
                        ids.forEach(TestnetPaperTrading::runSimulation);
                    } catch (Exception e) {
                        executorService.shutdown();
                        saveState(traders);
                        e.printStackTrace();
                    }
                }, 0, delay, TimeUnit.SECONDS);*/
    }

    private static void runSimulation(String id) {
        // get all positions to map where key is symbol
        // check if there are positions to execute
        // init balance , and it's logic per order
        // check if trader has new orders then emulate
    }

    private static void executeOrder() {
        // calc pnl
        // change balance
        // delete position from list
    }

    private static void emulateOrder() {
        // save all data to dto
        // work with balance
    }

    private static void saveState() {

    }

    /**
     * Calculates position's pnl.
     * @return double value
     */
    private static double getPnl(TradeDtoData position) {
        double markPrice;
        return 0;
    }

    @SneakyThrows
    private static Logger initLogger() {
        Logger logger = Logger.getLogger("PaperTradingTestnet");
        FileHandler fh = new FileHandler("testnet_log.txt", true);
        fh.setFormatter(new Formatter() {
            @NotNull
            @Override
            public String format(@NotNull LogRecord record) {
                return String.format(new Date() + " "
                        + record.getLevel() + " " + record.getMessage() + "\n");
            }
        });
        logger.addHandler(fh);
        return logger;
    }
}
