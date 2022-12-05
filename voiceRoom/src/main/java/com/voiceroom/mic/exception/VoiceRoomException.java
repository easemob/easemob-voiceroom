package com.voiceroom.mic.exception;

import org.springframework.http.HttpStatus;

public class VoiceRoomException extends RuntimeException {

    private String code;

    private String message;

    private HttpStatus httpStatus;

    public VoiceRoomException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
