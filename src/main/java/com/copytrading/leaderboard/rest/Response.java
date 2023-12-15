package com.copytrading.leaderboard.rest;

import lombok.Data;

import java.util.Map;

@Data
public class Response {
    private String response;
    private Map<String, String> headers;
    private String sessionId;

    public Response(String response, Map<String, String> headers, String sessionId) {
        super();
        this.response = response;
        this.headers = headers;
        this.sessionId = sessionId;
    }

    public Response(String response, Map<String, String> headers) {
        this(response, headers, null);
    }

}
