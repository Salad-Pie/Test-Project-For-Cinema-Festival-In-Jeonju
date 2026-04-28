package com.example.springboot.controller;

import com.example.springboot.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String field = fieldError == null ? null : fieldError.getField();
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = fieldError == null || fieldError.getDefaultMessage() == null
                    ? "invalid request."
                    : fieldError.getDefaultMessage();
        }

        return ResponseEntity.badRequest().body(new ApiErrorResponse("VALIDATION_ERROR", message, field));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "unexpected server error.";
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_SERVER_ERROR", message, null));
    }
}
