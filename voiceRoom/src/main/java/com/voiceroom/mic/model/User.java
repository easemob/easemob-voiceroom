package com.voiceroom.mic.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.voiceroom.common.util.MdStringUtils;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.codec.digest.Md5Crypt;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.apache.commons.codec.Charsets.UTF_8;

@Value
@TableName("user")
@Builder(toBuilder = true)
public class User implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private String uid;

    private String name;

    private String portrait;

    private String deviceId;

    private String phone;

    @JsonCreator
    public User(Integer id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String uid,
            String name,
            String portrait, String deviceId, String phone) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.uid = uid;
        this.name = name;
        this.portrait = portrait;
        this.deviceId = deviceId;
        this.phone = phone;
    }

    public static User create(String name, String deviceId, String portrait) {
        return create(name, deviceId, portrait, null);
    }

    public static User create(String name, String deviceId, String portrait, String phone) {
        String uid = buildUid(name, deviceId);
        return User.builder().uid(uid)
                .name(name)
                .deviceId(deviceId)
                .portrait(portrait)
                .phone(phone)
                .build();
    }

    private static String buildUid(String name, String deviceId) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String s = name + deviceId + System.currentTimeMillis();
            String encode = Base64.getUrlEncoder().encodeToString(md5.digest(s.getBytes(UTF_8)));
            return MdStringUtils.randomDelete(encode, 5);
        } catch (NoSuchAlgorithmException e) {
            String s = name + deviceId + System.currentTimeMillis();
            String md5Str = Md5Crypt.md5Crypt(s.getBytes(UTF_8));
            return MdStringUtils.randomDelete(md5Str, 5);
        }
    }
}
