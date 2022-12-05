package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicApplyRepeatException extends VoiceRoomException {

    public MicApplyRepeatException() {
        super(ErrorCodeConstant.micRepeatApplyError, "addMicApply repeat error", HttpStatus.BAD_REQUEST);
    }
}
