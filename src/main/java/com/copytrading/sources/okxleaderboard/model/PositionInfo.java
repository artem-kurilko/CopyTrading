package com.copytrading.sources.okxleaderboard.model;

import lombok.Data;

@Data
public class PositionInfo {
    private String instId;
    private double availSubPos;
    private double closePnl;
    private double pnl;
    private double lever;
    private double openAvgPx;
    private double markPx;
    private String posSide;
    private String side;
    private double subPos;
    private String uniqueName;
    private String uTime;
}
