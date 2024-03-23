package com.copytrading.sources.okxleaderboard.model;

import lombok.Data;

@Data
public class InfoDto {
    private String desc;
    private String functionId;
    private String learnMoreUrl;
    private double order;
    private String title;
    private double type;
    private double value;
}
