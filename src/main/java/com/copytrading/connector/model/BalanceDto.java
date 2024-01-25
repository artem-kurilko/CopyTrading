package com.copytrading.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceDto {
    private String accountAlias;
    private Double maxWithdrawAmount;
    private Double balance;
    private Double crossWalletBalance;
    private Double crossUnPnl;
    private long updateTime;
    private String asset;
    private boolean marginAvailable;
    private Double availableBalance;
}
