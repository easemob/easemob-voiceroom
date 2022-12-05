package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class LoginResponse {

    private String uid;

    private String name;

    private String portrait;

    @JsonProperty("chat_uid")
    private String chatUid;

    private String authorization;

    @JsonProperty("im_token")
    private String imToken;

    @JsonProperty("rtc_uid")
    private Integer rtcUid;

}
