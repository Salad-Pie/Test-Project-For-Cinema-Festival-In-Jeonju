package com.example.springboot.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "이미 신청된 내역이 있습니다. 잠시 후 다시 시도해 주세요."),
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "같은 시간에 이미 신청된 정보가 있습니다."),
    DUPLICATE_SURVEY(HttpStatus.CONFLICT, "이미 설문에 참여한 내역이 있습니다. 잠시 후 다시 시도해 주세요."),
    CAPACITY_FULL(HttpStatus.CONFLICT, "해당 시간은 신청이 마감되었습니다."),
    INVALID_RESERVATION_SLOT(HttpStatus.BAD_REQUEST, "신청 가능한 날짜와 시간을 확인해 주세요."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "JPG, JPEG, PNG 파일만 업로드할 수 있습니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일을 확인해 주세요."),
    STORAGE_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "파일 저장 설정이 필요합니다. 관리자에게 문의해 주세요."),
    OCR_RECOGNITION_FAILED(HttpStatus.BAD_REQUEST, "서명을 인식하지 못했습니다. 조금 더 또렷하게 다시 작성해 주세요.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
