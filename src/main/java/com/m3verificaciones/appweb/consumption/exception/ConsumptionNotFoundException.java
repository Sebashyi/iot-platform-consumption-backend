package com.m3verificaciones.appweb.consumption.exception;

import org.springframework.http.HttpStatus;

public class ConsumptionNotFoundException extends ConsumptionException {
    public ConsumptionNotFoundException(String id) {
        super("Consumption record not found with ID: " + id, HttpStatus.NOT_FOUND);
    }

    public ConsumptionNotFoundException(String id, Throwable cause) {
        super("Consumption record not found with ID: " + id, HttpStatus.NOT_FOUND, cause);
    }
}