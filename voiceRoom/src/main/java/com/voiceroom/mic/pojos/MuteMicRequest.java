package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class MuteMicRequest {

    @NotNull(message = "mic_index must not be null")
    @JsonProperty("mic_index")
    private Integer micIndex;

    @JsonCreator
    public MuteMicRequest(@JsonProperty("mic_index") Integer micIndex) {
        this.micIndex = micIndex;
    }
}
