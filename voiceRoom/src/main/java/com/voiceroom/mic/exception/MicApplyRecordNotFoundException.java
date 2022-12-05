package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicApplyRecordNotFoundException extends VoiceRoomException {

    public MicApplyRecordNotFoundException() {
        super(ErrorCodeConstant.micApplyRecordNotFoundError, "mic apply record not found",
                HttpStatus.NOT_FOUND);
    }
}
