package com.voiceroom.mic.pojos.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voiceroom.mic.pojos.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MicApplyVO {

    @JsonProperty("mic_index")
    private Integer micIndex;

    private UserDTO member;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonCreator
    public MicApplyVO(@JsonProperty("mic_index") Integer micIndex, UserDTO member,
            @JsonProperty("created_at") Long createdAt) {
        this.micIndex = micIndex;
        this.member = member;
        this.createdAt = createdAt;
    }
}
