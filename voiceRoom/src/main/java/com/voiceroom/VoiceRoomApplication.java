package com.voiceroom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.voiceroom")
@EnableScheduling
@MapperScan("com.voiceroom.*.repository")
@EnableDiscoveryClient
public class VoiceRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceRoomApplication.class, args);
    }

}
