package com.voiceroom.mic.common.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class RedissonConfiguration {

    @Resource
    private RedisProperties redisProperties;

    @Bean(name = "voiceRoomRedisson")
    public RedissonClient voiceRoomRedisson() {
        Config config = new Config();
        if (redisProperties.getCluster() != null) {
            config.useClusterServers()
                    .addNodeAddress(redisProperties.getCluster().getNodes().toArray(new String[0]));
        } else {
            String url = String.format("redis://:%s@%s:%d", redisProperties.getPassword(),
                    redisProperties.getHost(), redisProperties.getPort());
            config.useSingleServer().setAddress(url);
        }
        if (StringUtils.isNotEmpty(redisProperties.getPassword())) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

}
