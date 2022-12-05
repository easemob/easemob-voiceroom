package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
public class SendSmsRequest {

    @JsonProperty("phone_number")
    @NotEmpty(message = "Phone number cannot be empty.")
    private String phoneNumber;

    @JsonCreator
    public SendSmsRequest(@JsonProperty("phone_number") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
