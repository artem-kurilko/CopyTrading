package com.copytrading.sources.copytradingleaderboard.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEntity {
    private String code;
    private String message;
    private String messageDetail;
    private boolean success;
}
