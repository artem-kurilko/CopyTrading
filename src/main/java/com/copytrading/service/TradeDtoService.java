package com.copytrading.service;

import com.copytrading.model.TradeDto;
import com.copytrading.repository.TradeDtoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeDtoService {
    private final TradeDtoRepository tradeDtoRepository;

    @Autowired
    public TradeDtoService(TradeDtoRepository tradeDtoRepository) {
        this.tradeDtoRepository = tradeDtoRepository;
    }

    public void save(TradeDto tradeDto) {
        tradeDtoRepository.save(tradeDto);
    }

    public void saveAll(List<TradeDto> tradesDtoList) {
        tradeDtoRepository.saveAll(tradesDtoList);
    }

    public List<TradeDto> findAll() {
        return tradeDtoRepository.findAll();
    }

    public TradeDto getOne(String id) {
        return tradeDtoRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public void delete(String id) {
        tradeDtoRepository.deleteById(id);
    }
}
