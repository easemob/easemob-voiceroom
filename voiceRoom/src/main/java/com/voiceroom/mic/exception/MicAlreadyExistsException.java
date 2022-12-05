package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicAlreadyExistsException extends VoiceRoomException{
    public MicAlreadyExistsException(String message) {
        super(ErrorCodeConstant.micInitAlreadyExists, message, HttpStatus.ALREADY_REPORTED);
    }
}
