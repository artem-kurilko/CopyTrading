package com.copytrading.controller;

import com.copytrading.model.PositionSide;
import com.copytrading.model.TradeDto;
import com.copytrading.model.TradeDtoData;
import com.copytrading.service.TradeDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TestController {
    private final TradeDtoService tradeDtoService;

    @Autowired
    public TestController(TradeDtoService tradeDtoService) {
        this.tradeDtoService = tradeDtoService;
    }

    @GetMapping
    public String sayHi() {
        tradeDtoService.save(TradeDto.builder()
                .traderId(UUID.randomUUID().toString())
                .positions(List.of(TradeDtoData.builder()
                        .symbol("BTCUSDT")
                        .side(PositionSide.LONG)
                        .time(System.currentTimeMillis())
                        .build()))
                .build());
        tradeDtoService.save(TradeDto.builder()
                .traderId(UUID.randomUUID().toString())
                .positions(List.of(TradeDtoData.builder()
                        .symbol("BTCUSDT")
                        .side(PositionSide.LONG)
                        .time(System.currentTimeMillis())
                        .build()))
                .build());
        tradeDtoService.save(TradeDto.builder()
                .traderId(UUID.randomUUID().toString())
                .positions(List.of(TradeDtoData.builder()
                        .symbol("BTCUSDT")
                        .side(PositionSide.LONG)
                        .time(System.currentTimeMillis())
                        .build()))
                .build());
        System.out.println(tradeDtoService.findAll());
        System.out.println(tradeDtoService.findAll().size());
        return  "Hi";
    }
}
