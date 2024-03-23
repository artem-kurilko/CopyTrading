package com.copytrading.sources.okxleaderboard.model;

import lombok.Getter;

@Getter
public enum TimePeriod {
    WEEK(7),
    MONTH(30),
    THREE_MONTHS(90),
    YEAR(0);

    private final int value;

    TimePeriod(int value) {
        this.value = value;
    }
}
