package com.voiceroom.mic.exception;

import com.voiceroom.mic.common.constants.ErrorCodeConstant;
import org.springframework.http.HttpStatus;

public class MicNotCurrentUserException extends VoiceRoomException {

    public MicNotCurrentUserException() {
        super(ErrorCodeConstant.micNotCurrentUser, "mic not current user",
                HttpStatus.BAD_REQUEST);
    }
}
