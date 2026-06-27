package com.docbranch.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_ACCESS_DENIED", "프로젝트 권한이 없습니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다."),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROJECT_MEMBER_ALREADY_EXISTS", "이미 프로젝트 멤버입니다."),
    PROJECT_INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_INVITATION_NOT_FOUND", "프로젝트 초대를 찾을 수 없습니다."),
    PROJECT_INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, "PROJECT_INVITATION_EXPIRED", "만료된 프로젝트 초대입니다."),
    PROJECT_INVITATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PROJECT_INVITATION_ALREADY_PROCESSED", "이미 처리된 프로젝트 초대입니다."),
    DOCUMENT_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCUMENT_DETAIL_NOT_FOUND", "문서 상세를 찾을 수 없습니다."),
    DOCUMENT_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCUMENT_VERSION_NOT_FOUND", "문서 버전을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
