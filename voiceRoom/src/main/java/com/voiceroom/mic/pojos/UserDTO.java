package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voiceroom.mic.model.User;
import com.voiceroom.mic.model.UserThirdAccount;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private String uid;

    @JsonProperty("chat_uid")
    private String chatUid;

    @JsonIgnore
    private String chatUuid;

    private String name;

    private String portrait;

    @JsonProperty("rtc_uid")
    private Integer rtcUid;

    @JsonProperty("mic_index")
    private Integer micIndex;

    @JsonCreator
    public UserDTO(@JsonProperty("uid") String uid,
            @JsonProperty("chat_uid") String chatUid,
            @JsonProperty("chat_uuid") String chatUuid,
            @JsonProperty("name") String name,
            @JsonProperty("portrait") String portrait,
            @JsonProperty("rtc_uid") Integer rtcUid,
            @JsonProperty("mic_index") Integer micIndex) {
        this.uid = uid;
        this.chatUid = chatUid;
        this.chatUuid = chatUuid;
        this.name = name;
        this.portrait = portrait;
        this.rtcUid = rtcUid;
        this.micIndex = micIndex;
    }

    public static UserDTO from(User user, UserThirdAccount userThirdAccount, Integer micIndex) {
        return UserDTO.builder()
                .uid(user.getUid())
                .chatUid(userThirdAccount.getChatId())
                .chatUuid(userThirdAccount.getChatUuid())
                .rtcUid(userThirdAccount.getRtcUid())
                .name(user.getName())
                .micIndex(micIndex)
                .portrait(user.getPortrait())
                .build();
    }

}
