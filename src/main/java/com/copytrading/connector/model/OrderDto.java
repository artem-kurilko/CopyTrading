package com.copytrading.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {
    private String orderId;
    private String clientOrderId;
    private String symbol;
    private String cumQuote;
    private String executedQty;
    private String orgQty;
    private String origType;
    private String price;
    private String side;
    private String positionSide;
    private String status;
    private String stopPrice;
    private String closePosition;
    private long time;
    private long updateTime;
    private String activatePrice;
}
