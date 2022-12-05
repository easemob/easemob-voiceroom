package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends VoiceRoomException {

    public UserNotFoundException() {
        super(ErrorCodeConstant.userNotFound, "user must not be null", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(ErrorCodeConstant.userNotFound, message, HttpStatus.NOT_FOUND);
    }
}
