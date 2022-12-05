package com.voiceroom.mic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easemob.im.server.EMException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceroom.common.im.ImApi;
import com.voiceroom.mic.exception.UserNotFoundException;
import com.voiceroom.mic.exception.VoiceRoomException;
import com.voiceroom.mic.model.User;
import com.voiceroom.mic.model.UserThirdAccount;
import com.voiceroom.mic.pojos.UserDTO;
import com.voiceroom.mic.repository.UserMapper;
import com.voiceroom.mic.service.UserService;
import com.voiceroom.mic.service.UserThirdAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String USER_RTC_RECORD_KEY = "voiceRoom:user:rtc:record";

    @Resource
    private ImApi imApi;

    @Resource
    private UserThirdAccountService userThirdAccountService;

    @Resource(name = "voiceRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${voice.room.redis.cache.ttl:PT1H}")
    private Duration ttl;

    @Override
    @Transactional
    public UserDTO loginDevice(String deviceId, String name, String portrait) {
        return loginDeviceWithPhone(deviceId, name, portrait, null);
    }

    @Override
    @Transactional
    public UserDTO loginDeviceWithPhone(String deviceId, String name, String portrait, String phone) {
        LambdaQueryWrapper<User> queryWrapper =
                new LambdaQueryWrapper<User>().eq(User::getDeviceId, deviceId);
        User user = getOne(queryWrapper);
        UserThirdAccount userThirdAccount;
        if (user == null) {
            if (StringUtils.isBlank(name) || StringUtils.isBlank(portrait)) {
                throw new IllegalArgumentException(
                        "create user the name and portrait must not be null");
            }
            String chatUid = String.format("u%s",
                    UUID.randomUUID().toString().replace("-", "")
                            .substring(1));
            user = User.create(name, deviceId, portrait, phone);
            save(user);
            try {
                userThirdAccount = imApi.createUser(user.getUid(), chatUid);
            } catch (EMException e) {
                throw new VoiceRoomException("100500", "create easemob user failed",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Long rtcUid = redisTemplate.opsForValue().increment(USER_RTC_RECORD_KEY);
            userThirdAccount = userThirdAccount.toBuilder().rtcUid(Math.toIntExact(rtcUid)).build();
            try {
                userThirdAccountService.save(userThirdAccount);
            } catch (Exception e) {
                log.error("save easemob user failed | err=", e);
                imApi.deleteUser(chatUid);
                throw e;
            }
        }
        else {
            String uid = user.getUid();
            boolean isUpdate = false;
            User update = user;
            if (StringUtils.isNotBlank(name)) {
                update = update.toBuilder().name(name).build();
                isUpdate = true;
            }
            if (StringUtils.isNotBlank(portrait)) {
                isUpdate = true;
                update = update.toBuilder().portrait(portrait).build();
            }
            if (StringUtils.isNotBlank(phone)) {
                isUpdate = true;
                update = update.toBuilder().phone(phone).build();
            }
            if (isUpdate && !update.equals(user)) {
                updateById(update);
                user = update;
                redisTemplate.delete(key(uid));
            }
            userThirdAccount = userThirdAccountService.getByUid(uid);
            if (userThirdAccount.getRtcUid() == null) {
                Long rtcUid = redisTemplate.opsForValue().increment(USER_RTC_RECORD_KEY);
                userThirdAccount =
                        userThirdAccount.toBuilder().rtcUid(Math.toIntExact(rtcUid)).build();
                userThirdAccountService.updateById(userThirdAccount);
            }
        }
        return UserDTO.builder().uid(user.getUid())
                .chatUid(userThirdAccount.getChatId())
                .chatUuid(userThirdAccount.getChatUuid())
                .rtcUid(userThirdAccount.getRtcUid())
                .name(user.getName())
                .portrait(user.getPortrait())
                .build();
    }

    @Override public Map<String, UserDTO> findByUidList(List<String> ownerUidList) {
        if (ownerUidList == null || ownerUidList.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<User> queryWrapper =
                new LambdaQueryWrapper<User>().in(User::getUid, ownerUidList);
        List<User> userList = baseMapper.selectList(queryWrapper);
        if (userList == null || userList.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, UserDTO> map = new HashMap<>(userList.size());
        for (User user : userList) {
            String uid = user.getUid();
            UserDTO userDTO = UserDTO.builder()
                    .uid(uid)
                    .portrait(user.getPortrait())
                    .name(user.getName())
                    .build();
            map.put(uid, userDTO);
        }
        return map;
    }

    @Override public UserDTO getByUid(String uid) {
        if (StringUtils.isBlank(uid)) {
            throw new IllegalArgumentException("uid must not be empty");
        }
        Boolean hasKey = redisTemplate.hasKey(key(uid));
        User user = null;
        if (Boolean.TRUE.equals(hasKey)) {
            String json = redisTemplate.opsForValue().get(key(uid));
            try {
                user = objectMapper.readValue(json, User.class);
            } catch (JsonProcessingException e) {
                log.error("parse user json cache failed | uid={}, json={}, e=", uid,
                        json, e);
            }
        }
        if (user == null) {
            LambdaQueryWrapper<User> queryWrapper =
                    new LambdaQueryWrapper<User>().eq(User::getUid, uid);
            user = baseMapper.selectOne(queryWrapper);
            try {
                String json = objectMapper.writeValueAsString(user);
                redisTemplate.opsForValue().set(key(uid), json, ttl);
            } catch (JsonProcessingException e) {
                log.error("write user json cache failed | uid={}, user={}, e=", uid,
                        user, e);
            }
        }
        if (user == null) {
            throw new UserNotFoundException("user " + uid + " not found");
        }
        UserThirdAccount userThirdAccount = userThirdAccountService.getByUid(uid);
        return UserDTO.from(user, userThirdAccount, null);
    }

    private String key(String uid) {
        return String.format("voiceRoom:user:uid:%s", uid);
    }

}
