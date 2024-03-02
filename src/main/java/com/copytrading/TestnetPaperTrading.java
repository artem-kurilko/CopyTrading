package com.copytrading;

import com.copytrading.model.TradeDto;
import com.copytrading.repository.TradeDtoRepository;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SpringBootApplication
public class TestnetPaperTrading {
    private static final Logger log = initLogger();

    @Autowired
    private static TradeDtoRepository tradeDtoRepository;
    private static HashMap<String, LinkedList<TradeDto>> traders;
    private static final int delay = 20;

    public static void mafin(String[] args) {
        initTradersPositions();
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
                }, 0, delay, TimeUnit.SECONDS);
    }

    private static void runSimulation(String id) {
        // get all positions to map where key is symbol
        // check if there are positions to execute
        // init balance , and it's logic per order
        // check if trader has new orders then emulate
    }

    private static void executeOrder() {

    }

    private static void emulateOrder() {

    }

    private static void saveState(HashMap<String, LinkedList<TradeDto>> traders) {
        tradeDtoRepository.deleteAll();
        List<TradeDto> trades = traders.values().stream().flatMap(Collection::stream).toList();
        tradeDtoRepository.saveAll(trades);
        long size = tradeDtoRepository.findAll().size();
        int tradersSize = traders.keySet().size();
        log.info("Saved all records. Traders: " + tradersSize + " Current positions: " + size);
    }

    private static void initTradersPositions() {
        HashMap<String, LinkedList<TradeDto>> traders = new HashMap<>();
        List<TradeDto> tradeDtoList = tradeDtoRepository.findAll();
        for (TradeDto tradeDto : tradeDtoList) {
            if (traders.get(tradeDto.getTraderId()) == null) {
                traders.put(tradeDto.getTraderId(), new LinkedList<>(List.of(tradeDto)));
            } else {
                traders.get(tradeDto.getTraderId()).add(tradeDto);
            }
        }
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
