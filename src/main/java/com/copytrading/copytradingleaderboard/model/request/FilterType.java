package com.copytrading.copytradingleaderboard.model.request;

/**
 * To filter leaderboard copy traders by.
 */
public enum FilterType {
    AUM,
    PNL,
    ROI,
    MDD,
    COPY_COUNT,
    COPIER_PNL, // good
    SHARP_RATIO // bad. seems like doesn't matter, traders that copy leaders by sharp have negative pnl
}
