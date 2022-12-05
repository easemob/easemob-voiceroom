package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.pojos.vo.GiftRecordVO;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceRoomDTO {

    @JsonProperty("room_id")
    private String roomId;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("chatroom_id")
    private String chatroomId;

    private String name;

    private Integer type;

    @JsonProperty("is_private")
    private Boolean isPrivate;

    @JsonProperty("allowed_free_join_mic")
    private Boolean allowFreeJoinMic;

    private UserDTO owner;

    @JsonProperty("member_count")
    private Long memberCount;

    @JsonProperty("click_count")
    private Long clickCount;

    @JsonProperty("gift_amount")
    private Long giftAmount;

    private String announcement;

    @JsonProperty("ranking_list")
    private List<GiftRecordVO> rankingList;

    @JsonProperty("use_robot")
    private Boolean useRobot;

    @JsonProperty("robot_volume")
    private Integer robotVolume;

    @JsonProperty("sound_effect")
    private String soundEffect;

    public static VoiceRoomDTO from(VoiceRoom voiceRoom, UserDTO owner, Long memberCount,
            Long clickCount, Long giftAmount) {
        return new VoiceRoomDTO(voiceRoom.getRoomId(), voiceRoom.getChannelId(),
                voiceRoom.getChatroomId(), voiceRoom.getName(), voiceRoom.getType(),
                voiceRoom.getIsPrivate(), voiceRoom.getAllowedFreeJoinMic(), owner, memberCount,
                clickCount, giftAmount, voiceRoom.getAnnouncement(), Collections.emptyList(),
                voiceRoom.getUseRobot(), voiceRoom.getRobotVolume(), voiceRoom.getSoundEffect());
    }
}
