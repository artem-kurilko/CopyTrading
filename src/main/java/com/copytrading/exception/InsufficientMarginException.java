package com.copytrading.exception;

public class InsufficientMarginException extends RuntimeException {

    public InsufficientMarginException(String message) {
        super(message);
    }
}
