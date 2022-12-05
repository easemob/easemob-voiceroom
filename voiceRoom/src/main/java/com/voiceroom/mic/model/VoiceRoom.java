package com.voiceroom.mic.model;

import com.baomidou.mybatisplus.annotation.*;
import com.voiceroom.common.util.MdStringUtils;
import com.voiceroom.mic.exception.VoiceRoomTypeMismatchException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.codec.digest.Md5Crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.apache.commons.codec.Charsets.UTF_8;

@Value
@EqualsAndHashCode
@TableName("voice_room")
@Builder(toBuilder = true)
public class VoiceRoom {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private String name;

    private String roomId;

    private String chatroomId;

    private String channelId;

    private Boolean isPrivate;

    private String password;

    private Boolean allowedFreeJoinMic;

    private Integer type;

    private String owner;

    private String soundEffect;

    private String announcement;

    private Boolean useRobot;

    private Integer micCount;

    private Integer robotCount;

    private Integer robotVolume;

    public static VoiceRoom create(String name, String chatroomId, Boolean isPrivate,
            String password, Boolean allowedFreeJoinMic, Integer type, String owner,
            String soundEffect, Boolean useRobot, Integer micCount, Integer robotCount,
            Integer robotVolume) {
        String roomId = buildRoomId(name);
        String channelId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        checkType(type);
        return VoiceRoom.builder().name(name).roomId(roomId).chatroomId(chatroomId)
                .channelId(channelId).isPrivate(isPrivate).password(password)
                .allowedFreeJoinMic(allowedFreeJoinMic).type(type)
                .owner(owner).soundEffect(soundEffect).useRobot(useRobot)
                .micCount(micCount).robotCount(robotCount).robotVolume(robotVolume)
                .build();
    }

    private static String buildRoomId(String name) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String s = name + System.currentTimeMillis();
            String encode = Base64.getUrlEncoder().encodeToString(md5.digest(s.getBytes(UTF_8)));
            return MdStringUtils.randomDelete(encode, 5);
        } catch (NoSuchAlgorithmException e) {
            String s = name + System.currentTimeMillis();
            String md5Str = Md5Crypt.md5Crypt(s.getBytes(UTF_8));
            return MdStringUtils.randomDelete(md5Str, 5);
        }
    }

    public VoiceRoom updateName(String name) {
        return this.toBuilder().name(name).build();
    }

    public VoiceRoom updateIsPrivate(Boolean isPrivate) {
        return this.toBuilder().isPrivate(isPrivate).build();
    }

    public VoiceRoom updatePassword(String password) {
        return this.toBuilder().password(password).build();
    }

    public VoiceRoom updateAllowedFreeJoinMic(Boolean allowedFreeJoinMic) {
        return this.toBuilder().allowedFreeJoinMic(allowedFreeJoinMic).build();
    }

    public VoiceRoom updateType(Integer type) {
        checkType(type);
        return this.toBuilder().type(type).build();

    }

    public VoiceRoom updateAnnouncement(String announcement) {
        return this.toBuilder().announcement(announcement).build();
    }

    public VoiceRoom updateUseRobot(Boolean useRobot) {
        return this.toBuilder().useRobot(useRobot).build();
    }

    public VoiceRoom updateMicCount(Integer micCount) {
        return this.toBuilder().micCount(micCount).build();
    }

    public VoiceRoom updateRobotCount(Integer robotCount) {
        return this.toBuilder().robotCount(robotCount).build();
    }

    public VoiceRoom updateRobotVolume(Integer robotVolume) {
        return this.toBuilder().robotVolume(robotVolume).build();
    }

    public VoiceRoom updateSoundEffect(String soundEffect) {
        return this.toBuilder().soundEffect(soundEffect).build();
    }

    private static void checkType(Integer type) {
        switch (type) {
            case 0:
            case 1:
                return;
            default:
                throw new VoiceRoomTypeMismatchException("room type mismatch");
        }
    }

}
