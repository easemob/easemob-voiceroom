package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MicInfo {

    @JsonProperty("mic_index")
    private Integer micIndex;

    private Integer status;

    private UserDTO member;

}
