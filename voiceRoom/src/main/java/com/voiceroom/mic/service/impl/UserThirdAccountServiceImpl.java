package com.voiceroom.mic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceroom.mic.model.UserThirdAccount;
import com.voiceroom.mic.repository.UserThirdAccountMapper;
import com.voiceroom.mic.service.UserThirdAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;

@Slf4j
@Service
public class UserThirdAccountServiceImpl extends ServiceImpl<UserThirdAccountMapper, UserThirdAccount>
        implements UserThirdAccountService {

    @Resource(name = "voiceRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${voice.room.redis.cache.ttl:PT1H}")
    private Duration ttl;

    @Override public UserThirdAccount getByUid(String uid) {
        Boolean hasKey = redisTemplate.hasKey(key(uid));
        UserThirdAccount userThirdAccount = null;
        if (Boolean.TRUE.equals(hasKey)) {
            String easemobUserStr = redisTemplate.opsForValue().get(key(uid));
            try {
                userThirdAccount = objectMapper.readValue(easemobUserStr, UserThirdAccount.class);
            } catch (JsonProcessingException e) {
                log.error("parse easemob user json cache failed | uid={}, str={}, e=", uid,
                        easemobUserStr, e);
            }
        }
        if (userThirdAccount == null) {
            LambdaQueryWrapper<UserThirdAccount> queryWrapper =
                    new LambdaQueryWrapper<UserThirdAccount>().eq(UserThirdAccount::getUid, uid);
            userThirdAccount = this.baseMapper.selectOne(queryWrapper);
            if (userThirdAccount != null) {
                String json;
                try {
                    json = objectMapper.writeValueAsString(userThirdAccount);
                    redisTemplate.opsForValue().set(key(uid), json, ttl);
                } catch (JsonProcessingException e) {
                    log.error("write easemob user json cache failed | uid={}, easemobUser={}, e=", uid,
                            userThirdAccount, e);
                }
            }
        }
        return userThirdAccount;
    }

    private String key(String uid) {
        return String.format("voiceRoom:easemobUser:uid:%s", uid);
    }

}
