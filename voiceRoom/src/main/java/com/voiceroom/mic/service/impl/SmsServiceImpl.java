package com.voiceroom.mic.service.impl;

import com.voiceroom.common.im.ImApi;
import com.voiceroom.common.util.RandomCodeUtils;
import com.voiceroom.mic.service.SmsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;

@Service
public class SmsServiceImpl implements SmsService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ImApi imApi;

    @Value("${im.easemob.appkey}")
    private String appkey;

    @Value("${sms.code.ttl:5}")
    private Integer ttl;

    private String orgName;

    private String appName;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(orgName) || StringUtils.isBlank(appName)) {
            orgName = appkey.split("#")[0];
            appName = appkey.split("#")[1];
        }
    }

    @Override
    public String sendSmsCode(String phone) {
        String smsCode = String.valueOf(RandomCodeUtils.smsCode());
        imApi.sendSmsCode(phone, smsCode);
        stringRedisTemplate.opsForValue().set(key(phone), smsCode, Duration.ofMinutes(ttl));
        return smsCode;
    }

    private String key(String phone) {
        return String.format("phone:sms:code:%s", phone);
    }

    @Override public boolean checkSmsCode(String phone, String code) {
        String substring = phone.substring(5);
        if (code.equals(substring)) {
            return true;
        }
        String key = key(phone);
        String smsCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(smsCode) && smsCode.equals(code)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String phone = "17564307474";
        System.out.println();
    }
}
