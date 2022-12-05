package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class InviteAgreeOnMicRequest {

    @JsonProperty("mic_index")
    private Integer micIndex;

    @JsonCreator
    public InviteAgreeOnMicRequest(
            @JsonProperty("mic_index") Integer micIndex) {
        this.micIndex = micIndex;
    }
}
