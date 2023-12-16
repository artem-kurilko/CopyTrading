package com.copytrading.leaderboard.futures.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Leader {
	private String encryptedUid;
	private String nickName;
	private long rank;
	private double value;
	private String twitterUrl;
	private long followerCount;
	private boolean positionShared;
}
