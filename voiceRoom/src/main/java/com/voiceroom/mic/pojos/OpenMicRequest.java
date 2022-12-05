package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.Size;

@Value
public class OpenMicRequest {

    @Size(max = 6)
    @JsonProperty("mic_index")
    private Integer micIndex;

    @JsonCreator
    public OpenMicRequest(@JsonProperty("mic_index") Integer micIndex) {
        this.micIndex = micIndex;
    }
}
