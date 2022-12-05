package com.voiceroom.mic.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class ExceptionResult {

    private String code;

    private String message;

    @JsonCreator
    public ExceptionResult(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
