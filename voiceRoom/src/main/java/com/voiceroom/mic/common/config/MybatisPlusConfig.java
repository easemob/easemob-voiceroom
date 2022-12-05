package com.voiceroom.mic.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Configuration
@MapperScan("com.voiceroom.mic.repository")
public class MybatisPlusConfig implements MetaObjectHandler {

    @Value("${local.zone.offset:+8}")
    private String zoneOffset;

    @Override public void insertFill(MetaObject metaObject) {

        setFieldValByName("createdAt", LocalDateTime.now(ZoneOffset.of(zoneOffset)), metaObject);
        setFieldValByName("updatedAt", LocalDateTime.now(ZoneOffset.of(zoneOffset)), metaObject);
    }

    @Override public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedAt", LocalDateTime.now(ZoneOffset.of(zoneOffset)), metaObject);
    }

}
