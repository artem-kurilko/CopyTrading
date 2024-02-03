package com.copytrading.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeadTraderState {
    private String traderId;
    private Double balance;
}
