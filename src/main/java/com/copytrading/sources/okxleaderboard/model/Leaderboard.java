package com.copytrading.sources.okxleaderboard.model;

import lombok.Data;

import java.util.List;

@Data
public class Leaderboard {
    private String dataVersion;
    private int pages;
    private List<LeadTrader> ranks;
    private int total;
}
