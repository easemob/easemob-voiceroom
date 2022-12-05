package com.voiceroom.mic.pojos;

import lombok.Value;

@Value
public class UpdateUserRequest {

    private String name;

    private String portrait;
}
