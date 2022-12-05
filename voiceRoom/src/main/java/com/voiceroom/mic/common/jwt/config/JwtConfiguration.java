package com.voiceroom.mic.common.jwt.config;

import com.voiceroom.mic.common.jwt.util.VoiceRoomJwtUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "jwt.token")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    public VoiceRoomJwtUtil voiceRoomJwtUtil() {
        return new VoiceRoomJwtUtil();
    }

}
