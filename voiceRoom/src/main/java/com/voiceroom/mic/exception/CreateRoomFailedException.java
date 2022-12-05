package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class CreateRoomFailedException extends VoiceRoomException {

    public CreateRoomFailedException(String message) {
        super(ErrorCodeConstant.createChatroomFailed, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
