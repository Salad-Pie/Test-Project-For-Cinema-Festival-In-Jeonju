package com.example.springboot.controller;

import com.example.springboot.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String BAD_REQUEST_MESSAGE = "요청 값을 확인해 주세요.";
    private static final String VALIDATION_ERROR_MESSAGE = "입력값을 확인해 주세요.";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.info("Handled bad request. message={}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", BAD_REQUEST_MESSAGE, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String field = fieldError == null ? null : fieldError.getField();

        log.info(
                "Handled validation error. field={} message={}",
                field,
                fieldError == null ? ex.getMessage() : fieldError.getDefaultMessage()
        );
        return ResponseEntity.badRequest().body(new ApiErrorResponse("VALIDATION_ERROR", VALIDATION_ERROR_MESSAGE, field));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.info("Handled unexpected error. type={} message={}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_SERVER_ERROR", INTERNAL_SERVER_ERROR_MESSAGE, null));
    }
}
