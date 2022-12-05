package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicApplyException extends VoiceRoomException {

    public MicApplyException() {
        super(ErrorCodeConstant.micApplyError, "addMicApply error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
