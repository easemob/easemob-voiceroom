package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class RefreshTokenRequest {

    @NotBlank(message = "uid must not be empty")
    @JsonProperty("uid")
    private String uid;

    @NotBlank(message = "device_id must not be empty")
    @JsonProperty("device_id")
    private String deviceId;

    @NotBlank(message = "token must not be empty")
    @JsonProperty("token")
    private String token;

    @JsonCreator
    public RefreshTokenRequest(@JsonProperty("uid") String uid,
            @JsonProperty("device_id") String deviceId,
            @JsonProperty("token") String token) {
        this.uid = uid;
        this.deviceId = deviceId;
        this.token = token;
    }
}
