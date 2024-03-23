package com.copytrading.sources.okxleaderboard.model;

import lombok.Data;

import java.util.List;

@Data
public class LeadTraderPerformance {
    private String symbol;
    private List<InfoDto> centers;
    private List<InfoDto> heads;
    private List<InfoDto> tails;
}
