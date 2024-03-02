package com.copytrading.repository;

import com.copytrading.model.TradeDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeDtoRepository extends MongoRepository<TradeDto, String> {
}
