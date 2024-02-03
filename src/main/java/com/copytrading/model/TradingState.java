package com.copytrading.model;

import lombok.Data;

import java.util.List;

/**
 * Simple java object to save bot's state: traders ids to follow, balance distribution, etc.
 * @see com.copytrading.service.MongoDBService
 */
@Data
public class TradingState {
    private List<LeadTraderState> leadTraderStates;
}
