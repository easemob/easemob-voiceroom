package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class GetVoiceRoomResponse {

    private VoiceRoomDTO room;

    @JsonProperty("mic_info")
    private List<MicInfo> micInfo;

}
