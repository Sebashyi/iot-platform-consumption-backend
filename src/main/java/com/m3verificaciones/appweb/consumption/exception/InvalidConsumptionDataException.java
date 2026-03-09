package com.m3verificaciones.appweb.consumption.exception;

import org.springframework.http.HttpStatus;

public class InvalidConsumptionDataException extends ConsumptionException {
    public InvalidConsumptionDataException(String field) {
        super("Invalid consumption data for field: " + field, HttpStatus.BAD_REQUEST);
    }

    public InvalidConsumptionDataException(String field, Throwable cause) {
        super("Invalid consumption data for field: " + field, HttpStatus.BAD_REQUEST, cause);
    }
}