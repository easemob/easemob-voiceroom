package com.voiceroom.mic.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

@Configuration
public class RedisConfiguration {

    @Value("${local.zone.offset:+8}")
    private String zoneOffset;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        //设置时区
        objectMapper.setTimeZone(
                TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.of(zoneOffset))));
        return objectMapper;
    }

    @Bean(name = "voiceRedisTemplate")
    public StringRedisTemplate voiceRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

}
