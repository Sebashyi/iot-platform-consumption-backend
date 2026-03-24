package com.m3verificaciones.appweb.consumption.exception;

import org.springframework.http.HttpStatus;

public class ConsumptionNoResultsException extends ConsumptionException {
    public ConsumptionNoResultsException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
    
}
