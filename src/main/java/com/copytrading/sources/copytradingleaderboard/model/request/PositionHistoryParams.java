package com.copytrading.sources.copytradingleaderboard.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionHistoryParams {
    private int pageNumber;
    private int pageSize;
    private String portfolioId;
}
