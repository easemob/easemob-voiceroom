package com.voiceroom.mic.service;

import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.pojos.MicInfo;
import com.voiceroom.mic.pojos.UserDTO;

import java.util.List;

public interface VoiceRoomMicService {

    List<MicInfo> getByRoomId(String roomId);

    List<MicInfo> getRoomMicInfo(VoiceRoom voiceRoom);

    Boolean setRoomMicInfo(VoiceRoom roomInfo, UserDTO user, Integer micIndex, boolean inOrder);

    List<MicInfo> initMic(VoiceRoom voiceRoom, Boolean isActive);

    void updateRobotMicStatus(VoiceRoom voiceRoom, Boolean isActive);

    void closeMic(UserDTO user, String chatroomId, Integer micIndex, String roomId);

    void openMic(UserDTO user, String chatroomId, Integer index, String roomId);

    void leaveMic(UserDTO user, String chatroomId, Integer index, String roomId);

    void muteMic(String chatroomId, Integer index, String roomId);

    void unMuteMic(String chatroomId, Integer index, String roomId);

    void kickUserMic(VoiceRoom voiceRoom, Integer index, String uid, String roomId);

    void lockMic(String chatroomId, Integer index, String roomId);

    void unLockMic(String chatroomId, Integer index, String roomId);

    void invite(VoiceRoom roomInfo, Integer index, String uid);

    Boolean agreeInvite(VoiceRoom roomInfo, UserDTO user, Integer micIndex);

    Boolean refuseInvite(VoiceRoom roomInfo, String uid);

    void exchangeMic(String chatroomId, Integer from, Integer to, String uid, String roomId);
}
