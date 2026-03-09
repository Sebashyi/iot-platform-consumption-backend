package com.m3verificaciones.appweb.consumption.exception;

import org.springframework.http.HttpStatus;

public class ConsumptionPersistenceException extends ConsumptionException {
    public ConsumptionPersistenceException(String operation) {
        super("Error during consumption record " + operation, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ConsumptionPersistenceException(String operation, Throwable cause) {
        super("Error during consumption record " + operation, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}