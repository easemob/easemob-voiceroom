package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class GetVoiceRoomRtcTokenResponse {

    @JsonProperty("rtc_token")
    private String rtcToken;

    @JsonCreator
    public GetVoiceRoomRtcTokenResponse(@JsonProperty("rtc_token") String rtcToken) {
        this.rtcToken = rtcToken;
    }
}
