package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class KickRoomRequest {

    private String uid;

    @JsonCreator
    public KickRoomRequest(String uid) {
        this.uid = uid;
    }
}
