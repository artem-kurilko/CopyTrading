package com.copytrading.sources.okxleaderboard.model;

import lombok.Data;

@Data
public class LeadTrader {
   private double aum;
   private double followPnl;
   private int followerNum;
   private String nickName;
   private double pnl;
   private String uniqueName;
   private double winRatio;
   private String yieldRatio;
}
