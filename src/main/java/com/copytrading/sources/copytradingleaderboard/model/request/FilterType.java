package com.copytrading.sources.copytradingleaderboard.model.request;

/**
 * To filter leaderboard copy traders by.
 */
public enum FilterType {
    AUM,
    PNL,
    ROI,
    MDD, // SHIT, never use. Best mdd only have those with 5-6 days of running trades
    COPY_COUNT,
    COPIER_PNL, // good
    SHARP_RATIO // bad. seems like doesn't matter, traders that copy leaders by sharp have negative pnl
}
