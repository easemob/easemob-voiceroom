package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class AddMicApplyResponse {

    @JsonProperty("result")
    private Boolean result;

    @JsonCreator
    public AddMicApplyResponse(@JsonProperty("result") Boolean result) {
        this.result = result;
    }
}
