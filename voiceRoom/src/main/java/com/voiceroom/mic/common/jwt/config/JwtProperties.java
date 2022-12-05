package com.voiceroom.mic.common.jwt.config;

import lombok.Data;

@Data
public class JwtProperties {

    private String secretKey;

    private Integer exTime;

}
