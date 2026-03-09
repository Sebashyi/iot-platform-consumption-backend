package com.m3verificaciones.appweb.consumption.exception;

import org.springframework.http.HttpStatus;

public class ConsumptionException extends RuntimeException {
    private final HttpStatus status;

    public ConsumptionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ConsumptionException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}