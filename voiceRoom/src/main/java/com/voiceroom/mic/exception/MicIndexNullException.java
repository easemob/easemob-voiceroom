package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicIndexNullException extends VoiceRoomException {

    public MicIndexNullException() {
        super(ErrorCodeConstant.micIndexNullError, "mic index is null", HttpStatus.BAD_REQUEST);
    }
}
