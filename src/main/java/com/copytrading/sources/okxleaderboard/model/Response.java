package com.copytrading.sources.okxleaderboard.model;

import lombok.Data;

import java.util.List;

@Data
public class Response<T> {
    private String code;
    private List<T> data;
    private String msg;
}
