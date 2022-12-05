package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class LockMicRequest {

    @NotNull(message = "lock mic_index must not be null")
    @JsonProperty("mic_index")
    private Integer micIndex;

    @JsonCreator
    public LockMicRequest(@JsonProperty("mic_index")Integer micIndex) {
        this.micIndex = micIndex;
    }
}
