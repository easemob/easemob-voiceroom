package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class LoginRequest {

    private String name;

    private String phone;

    @JsonProperty("verify_code")
    private String verifyCode;

    @NotBlank(message = "device_id must not be empty")
    @JsonProperty("device_id")
    private String deviceId;

    private String portrait;

}
