package com.copytrading.leaderboard.model;

public enum TimeRange {
    D7("7D"),
    D30("30D"),
    D90("90D");

    public final String value;

    TimeRange(String value) {
        this.value = value;
    }
}
