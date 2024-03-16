package com.copytrading.sources.binance.futuresleaderboard.model.response.position;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position {
	private String traderId;
	private String symbol;
	private double entryPrice;
	private double markPrice;
	private double pnl;
	private double roe;
	private int[] updateTime;
	private double amount;
	private long updateTimeStamp;
	private boolean yellow;
	private boolean tradeBefore;
	private int leverage;
}
