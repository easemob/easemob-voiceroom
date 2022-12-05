package com.voiceroom.common.im.config;

import com.easemob.im.server.EMProperties;
import com.easemob.im.server.EMService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "im.easemob")
    public ImProperties imProperties() {
        return new ImProperties();
    }

    @Bean
    public EMService emService(ImProperties imProperties) {
        EMProperties properties = EMProperties.builder()
                .setAppkey(imProperties.getAppkey())
                .setClientId(imProperties.getClientId())
                .setClientSecret(imProperties.getClientSecret())
                .setBaseUri(imProperties().getBaseUri())
                .setHttpConnectionPoolSize(imProperties.getHttpConnectionPoolSize())
                .build();
        return new EMService(properties);
    }

}
