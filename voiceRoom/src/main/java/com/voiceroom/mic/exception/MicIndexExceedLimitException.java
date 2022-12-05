package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicIndexExceedLimitException extends VoiceRoomException {

    public MicIndexExceedLimitException(String message) {
        super(ErrorCodeConstant.micIndexExceedLimitError, message, HttpStatus.BAD_REQUEST);
    }
}
