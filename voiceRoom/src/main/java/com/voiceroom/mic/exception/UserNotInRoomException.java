package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class UserNotInRoomException extends VoiceRoomException {

    public UserNotInRoomException() {
        super(ErrorCodeConstant.userNotInRoomError, "user not in voice room", HttpStatus.FORBIDDEN);
    }

    public UserNotInRoomException(String message) {
        super(ErrorCodeConstant.userNotInRoomError, message, HttpStatus.FORBIDDEN);
    }
}
