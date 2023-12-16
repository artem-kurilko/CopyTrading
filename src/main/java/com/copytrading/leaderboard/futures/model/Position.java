package com.copytrading.leaderboard.futures.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position implements Comparable<Position> {
	private int id;
	private String symbol;
	private double entryPrice;
	private double amount;
	private double markPrice;
	private double pnl;
	private double roe;
	private String updateTimeStamp;
	private String yellow;
	private String leader;
	private int status;

	public Position() {
		this.status = 1;
	}

	public Position(int id, String symbol, double entryPrice, double amount, double markPrice, double pnl, double roe,
                    String updateTimeStamp, String yellow, int status, String leader) {
		this.id = id;
		this.symbol = symbol;
		this.amount = amount;
		this.entryPrice = entryPrice;
		this.markPrice = markPrice;
		this.pnl = pnl;
		this.roe = roe;
		this.updateTimeStamp = updateTimeStamp;
		this.yellow = yellow;
		this.status = status;
		this.leader = leader;
	}

	public Position(Map<String, String> values) {
		this(Integer.parseInt(values.get("id")), values.get("symbol"), Double.parseDouble(values.get("entryPrice")),
				Double.parseDouble(values.get("amount")), Double.parseDouble(values.get("markPrice")),
				Double.parseDouble(values.get("pnl")), Double.parseDouble(values.get("roe")),
				values.get("updateTimeStamp"), values.get("yellow"), Integer.parseInt(values.get("status")),
				values.get("leader"));
	}

	@Override
	public int compareTo(Position o) {
		return symbol.compareTo(o.getSymbol());
	}
}
