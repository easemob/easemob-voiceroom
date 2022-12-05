package com.voiceroom.mic.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@TableName("user_third_account")
public class UserThirdAccount {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String uid;

    private String chatId;

    private String chatUuid;

    private Integer rtcUid;

    @JsonCreator
    public UserThirdAccount(Integer id, String uid, String chatId, String chatUuid, Integer rtcUid) {
        this.id = id;
        this.uid = uid;
        this.chatId = chatId;
        this.chatUuid = chatUuid;
        this.rtcUid = rtcUid;
    }
}
