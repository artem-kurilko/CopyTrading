package com.copytrading.sources.copytradingleaderboard.model.response.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraderData {
    private String userId;
    private boolean leadOwner;
    private boolean hasCopy;
    private String leadPortfolioId;
    private String nickname;
    private String nicknameTranslate;
    private String avatarUrl;
    private String status;
    private String description;
    private String descTranslate;
    private int favoriteCount;
    private int currentCopyCount;
    private int maxCopyCount;
    private int totalCopyCount;
    private String marginBalance;
    private String initInvestAsset;
    private String futuresType;
    private String aumAmount;
    private String copierPnl;
    private String copierPnlAsset;
    private String profitSharingRate;
    private String unrealizedProfitShareAmount;
    private long startTime;
    private long endTime;
    private long closedTime;
    private List<String> tag;
    private boolean positionShow;
    private int mockCopyCount;
    private String sharpRatio;
    private boolean hasMock;
    private long lockPeriod;
    private long copierLockPeriodTime;
    private boolean favorite;
}
