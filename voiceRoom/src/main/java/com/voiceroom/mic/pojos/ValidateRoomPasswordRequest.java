package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class ValidateRoomPasswordRequest {

    @NotNull(message = "password must not be null")
    private String password;

    @JsonCreator
    public ValidateRoomPasswordRequest(String password) {
        this.password = password;
    }
}
