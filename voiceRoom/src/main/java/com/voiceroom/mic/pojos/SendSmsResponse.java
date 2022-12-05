package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class SendSmsResponse {

    private boolean result;

    @JsonCreator
    public SendSmsResponse(boolean result) {
        this.result = result;
    }
}
