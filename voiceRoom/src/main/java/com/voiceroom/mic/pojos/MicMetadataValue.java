package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicMetadataValue {

    private UserDTO member;

    private Integer status;

    @JsonProperty("mic_index")
    private Integer micIndex;

    public String getUid() {
        return member == null ? null : member.getUid();
    }

}
