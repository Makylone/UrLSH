package com.Makylone.UrLSH.exception;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 1. Tells Spring this class handles errors globally
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class) // 2. Listen for this specific Java error
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        // 3. Return a nice JSON with 404 status
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }
}