package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicNotBelongYouException extends VoiceRoomException {

    public MicNotBelongYouException() {
        super(ErrorCodeConstant.micNotBelongYouError, "mic index not belong you",
                HttpStatus.UNAUTHORIZED);
    }
}
