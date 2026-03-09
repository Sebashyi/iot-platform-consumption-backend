package com.m3verificaciones.appweb.consumption.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ConsumptionExceptionHandler {

    @ExceptionHandler(ConsumptionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleConsumptionNotFound(ConsumptionNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatus().value());
        body.put("error", "Consumption Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(InvalidConsumptionDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidConsumptionData(InvalidConsumptionDataException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatus().value());
        body.put("error", "Invalid Consumption Data");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(ConsumptionPersistenceException.class)
    public ResponseEntity<Map<String, Object>> handleConsumptionPersistence(ConsumptionPersistenceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatus().value());
        body.put("error", "Consumption Persistence Error");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, ex.getStatus());
    }
}