package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class KickUserMicRequest {

    @NotNull(message = "kick mic_index must not be null")
    @JsonProperty("mic_index")
    private Integer micIndex;

    private String uid;

    @JsonCreator
    public KickUserMicRequest(@JsonProperty("mic_index") Integer micIndex, String uid) {
        this.micIndex = micIndex;
        this.uid = uid;
    }
}
