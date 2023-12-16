package com.copytrading.leaderboard.futures.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Leader {
	private String encryptedUid;
	private String nickName;
	private long rank;
	private double value;
	private String twitterUrl;

	public Leader(String encryptedUid, String nickName, long rank, double value, String twitterUrl) {
		super();
		this.encryptedUid = encryptedUid;
		this.nickName = nickName;
		this.rank = rank;
		this.value = value;
		this.twitterUrl = twitterUrl;
	}

	public Leader(Map<String, String> values) {
		this(values.get("encryptedUid"), values.get("nickName"), Long.parseLong(values.get("rank")),
				Double.parseDouble(values.get("value")), values.get("twitterUrl"));
	}

}
