package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class VoiceRoomSecurityException extends VoiceRoomException {

    public VoiceRoomSecurityException(String message) {
        super(ErrorCodeConstant.roomUnSupportedOperation, message,
                HttpStatus.UNAUTHORIZED);
    }
}
