package com.voiceroom.mic.service.impl;

import com.easemob.im.server.api.metadata.chatroom.AutoDelete;
import com.easemob.im.server.api.metadata.chatroom.get.ChatRoomMetadataGetResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceroom.common.im.ImApi;
import com.voiceroom.mic.common.constants.CustomEventType;
import com.voiceroom.mic.common.constants.MicOperateStatus;
import com.voiceroom.mic.common.constants.MicStatus;
import com.voiceroom.mic.exception.*;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.model.VoiceRoomUser;
import com.voiceroom.mic.pojos.MicInfo;
import com.voiceroom.mic.pojos.MicMetadataValue;
import com.voiceroom.mic.pojos.UserDTO;
import com.voiceroom.mic.service.*;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VoiceRoomMicServiceImpl implements VoiceRoomMicService {

    private static final String OPERATOR = "admin";

    private static final String METADATA_PREFIX_KEY = "mic";

    @Resource
    private ImApi imApi;

    @Resource
    private UserService userService;

    @Resource
    private VoiceRoomService voiceRoomService;

    @Resource
    private MicApplyUserService micApplyUserService;

    @Resource
    private VoiceRoomUserService voiceRoomUserService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource(name = "voiceRoomRedisson")
    private RedissonClient redisson;

    @Resource
    private PrometheusMeterRegistry registry;

    @Override
    public List<MicInfo> getByRoomId(String roomId) {
        return getRoomMicInfo(voiceRoomService.findByRoomId(roomId));
    }

    @Override
    public List<MicInfo> getRoomMicInfo(VoiceRoom voiceRoom) {

        String chatroomId = voiceRoom.getChatroomId();
        int micCount = voiceRoom.getMicCount() + voiceRoom.getRobotCount();
        List<String> allMics = new ArrayList<>();
        for (int index = 0; index < micCount; index++) {
            allMics.add(buildMicKey(index));
        }
        try {
            ChatRoomMetadataGetResponse chatRoomMetadataGetResponse =
                    imApi.listChatRoomMetadata(chatroomId, allMics);
            if (chatRoomMetadataGetResponse == null) {
                throw new VoiceRoomException("400500", "easemob service rquest failed",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Map<String, String> metadata = chatRoomMetadataGetResponse.getMetadata();
            List<MicInfo> micInfo = buildMicInfo(metadata);
            return micInfo;
        } catch (Exception e) {
            log.error("getRoomMicInfo error,roomId:{}", chatroomId, e);
            return Collections.emptyList();
        }

    }

    @Override
    public Boolean setRoomMicInfo(VoiceRoom roomInfo, UserDTO user, Integer micIndex,
            boolean inOrder) {

        boolean hasMic = false;

        if (micIndex == null && !inOrder) {
            throw new MicIndexNullException();
        }
        List<MicInfo> micInfos = this.getRoomMicInfo(roomInfo);
        Optional<MicInfo> micInfo = micInfos.stream().filter((mic) -> mic.getMember() != null
                && mic.getMember().getUid().equals(user.getUid())).findFirst();
        String chatroomId = roomInfo.getChatroomId();
        if (micInfo.isPresent()) {
            throw new MicAlreadyExistsException("mic user already exists");
        }

        Integer micCount = roomInfo.getMicCount();

        if (micIndex != null) {
            if (micIndex >= micCount) {
                throw new MicIndexExceedLimitException("mic index exceed the maximum");
            }
            if (0 > micIndex) {
                throw new MicIndexExceedLimitException("mic index exceed the minimum");
            }
            try {
                this.updateVoiceRoomMicInfo(chatroomId, user, micIndex,
                        MicOperateStatus.UP_MIC.getStatus(), Boolean.FALSE, roomInfo.getRoomId());
                hasMic = true;
            } catch (Exception e) {
                log.warn("on mic failure,chatroomId:{},user:{},index:{}", chatroomId, user,
                        micIndex,
                        e);
            }
        }

        if (!hasMic && inOrder) {

            //按顺序上麦
            for (int index = 1; index < micCount; index++) {
                try {
                    this.updateVoiceRoomMicInfo(chatroomId, user, index,
                            MicOperateStatus.UP_MIC.getStatus(), Boolean.FALSE,
                            roomInfo.getRoomId());
                    hasMic = true;
                    break;
                } catch (Exception e) {
                    log.warn("on mic failure,roomId:{},uid:{},index:{}", chatroomId, user, index,
                            e);
                }

            }
        }
        return hasMic;

    }

    @Override
    public List<MicInfo> initMic(VoiceRoom voiceRoom, Boolean isActive) {

        int micCount = voiceRoom.getMicCount() + voiceRoom.getRobotCount();

        String chatroomId = voiceRoom.getChatroomId();

        String ownerUid = voiceRoom.getOwner();

        try {
            Map<String, String> metadataMap = new HashMap<>();
            for (int micIndex = 0; micIndex < micCount; micIndex++) {
                String micKey = buildMicKey(micIndex);
                MicMetadataValue micMetadataValue;
                if (micIndex == 0) {
                    micMetadataValue = new MicMetadataValue(this.userService.getByUid(ownerUid),
                            MicStatus.NORMAL.getStatus(), micIndex);
                } else {
                    micMetadataValue =
                            new MicMetadataValue(null, MicStatus.FREE.getStatus(), micIndex);
                }
                if ((micIndex + voiceRoom.getRobotCount()) >= micCount) {
                    if (isActive) {
                        micMetadataValue =
                                new MicMetadataValue(null, MicStatus.ACTIVE.getStatus(), micIndex);
                    } else {
                        micMetadataValue =
                                new MicMetadataValue(null, MicStatus.INACTIVE.getStatus(),
                                        micIndex);
                    }
                }

                String jsonValue = "";
                try {
                    jsonValue = objectMapper.writeValueAsString(micMetadataValue);
                } catch (Exception e) {
                    log.error("write MicMetadataValue json failed | MicMetadataValue={}, err=",
                            micMetadataValue, e);
                }
                metadataMap.put(micKey, jsonValue);
            }
            //
            List<String> successKeys =
                    imApi.setChatRoomMetadata(OPERATOR, chatroomId, metadataMap,
                            AutoDelete.DELETE)
                            .getSuccessKeys();
            if (successKeys.size() != micCount) {
                imApi.deleteChatRoomMetadata(OPERATOR, chatroomId, successKeys);
                throw new MicInitException();
            }
            return buildMicInfo(metadataMap);
        } catch (Exception e) {
            log.error("init mic to easemob failed | roomId={}, err=", chatroomId, e);
            throw e;
        }
    }

    @Override
    public void updateRobotMicStatus(VoiceRoom voiceRoom, Boolean isActive) {
        Integer robotCount = voiceRoom.getRobotCount();
        Map<String, String> metadata = new HashMap<>();
        for (int index = 0; index < robotCount; index++) {
            String robotMetaDataKey = buildMicKey(voiceRoom.getMicCount() + index);
            MicMetadataValue micMetadataValue = new MicMetadataValue(null,
                    isActive ? MicStatus.ACTIVE.getStatus() : MicStatus.INACTIVE.getStatus(),
                    voiceRoom.getMicCount() + index);
            String jsonValue = "";
            try {
                jsonValue = objectMapper.writeValueAsString(micMetadataValue);
            } catch (Exception e) {
                log.error("write MicMetadataValue json failed | MicMetadataValue={}, err=",
                        micMetadataValue, e);
            }
            metadata.put(robotMetaDataKey, jsonValue);
        }
        imApi.setChatRoomMetadata(OPERATOR, voiceRoom.getChatroomId(), metadata,
                AutoDelete.DELETE)
                .getSuccessKeys();
    }

    @Override
    public void closeMic(UserDTO user, String chatroomId, Integer micIndex, String roomId) {

        this.updateVoiceRoomMicInfo(chatroomId, user, micIndex,
                MicOperateStatus.CLOSE_MIC.getStatus(), Boolean.FALSE, roomId);

    }

    @Override
    public void openMic(UserDTO user, String chatroomId, Integer micIndex, String roomId) {

        this.updateVoiceRoomMicInfo(chatroomId, user, micIndex,
                MicOperateStatus.OPEN_MIC.getStatus(), Boolean.FALSE, roomId);

    }

    @Override
    public void leaveMic(UserDTO user, String chatroomId, Integer micIndex, String roomId) {

        if (micIndex < 1) {
            return;
        }
        this.updateVoiceRoomMicInfo(chatroomId, user, micIndex,
                MicOperateStatus.LEAVE_MIC.getStatus(), Boolean.FALSE, roomId);

    }

    @Override
    public void muteMic(String chatroomId, Integer micIndex, String roomId) {
        this.updateVoiceRoomMicInfo(chatroomId, null, micIndex,
                MicOperateStatus.MUTE_MIC.getStatus(), Boolean.TRUE, roomId);
    }

    @Override
    public void unMuteMic(String chatroomId, Integer micIndex, String roomId) {

        this.updateVoiceRoomMicInfo(chatroomId, null, micIndex,
                MicOperateStatus.UNMUTE_MIC.getStatus(), Boolean.TRUE, roomId);

    }

    @Override
    public void kickUserMic(VoiceRoom roomInfo, Integer micIndex, String uid, String roomId) {

        VoiceRoomUser voiceRoomUser =
                voiceRoomUserService.findByRoomIdAndUid(roomInfo.getRoomId(), uid);

        if (voiceRoomUser == null || voiceRoomUser.getMicIndex() == null
                || voiceRoomUser.getMicIndex() == -1) {
            return;
        }

        this.updateVoiceRoomMicInfo(roomInfo.getChatroomId(), null, voiceRoomUser.getMicIndex(),
                MicOperateStatus.KICK_MIC.getStatus(), Boolean.TRUE, roomId);

    }

    @Override
    public void lockMic(String chatroomId, Integer micIndex, String roomId) {

        this.updateVoiceRoomMicInfo(chatroomId, null, micIndex,
                MicOperateStatus.LOCK_MIC.getStatus(), Boolean.TRUE, roomId);

    }

    @Override
    public void unLockMic(String chatroomId, Integer micIndex, String roomId) {

        this.updateVoiceRoomMicInfo(chatroomId, null, micIndex,
                MicOperateStatus.UNLOCK_MIC.getStatus(), Boolean.TRUE, roomId);

    }

    @Override
    public void invite(VoiceRoom roomInfo, Integer index, String uid) {
        UserDTO userDTO = this.userService.getByUid(uid);
        if (userDTO == null) {
            throw new UserNotFoundException();
        }
        UserDTO ownerUser = userService.getByUid(roomInfo.getOwner());

        Map<String, Object> customExtensions = new HashMap<>();
        String jsonUser = "";
        try {
            jsonUser = objectMapper.writeValueAsString(userDTO);
        } catch (Exception e) {
            log.error("write user json failed | uid={}, user={}, e=", uid,
                    userDTO, e);
        }
        customExtensions.put("user", jsonUser);
        if (index != null) {
            customExtensions.put("mic_index", index.toString());
        }
        customExtensions.put("room_id", roomInfo.getRoomId());
        this.imApi.sendUserCustomMessage(ownerUser.getChatUid(), userDTO.getChatUid(),
                CustomEventType.INVITE_SITE.getValue(), customExtensions, new HashMap<>());
    }

    @Override
    public Boolean agreeInvite(VoiceRoom roomInfo, UserDTO user, Integer micIndex) {

        Boolean result = false;
        if (micIndex == null) {
            result = setRoomMicInfo(roomInfo, user, null, Boolean.TRUE);
        } else {
            result = setRoomMicInfo(roomInfo, user, micIndex, Boolean.FALSE);
        }
        if (result) {
            micApplyUserService.deleteMicApply(user.getUid(), roomInfo, Boolean.FALSE);
        }
        return result;
    }

    @Override
    public Boolean refuseInvite(VoiceRoom roomInfo, String uid) {

        UserDTO userDTO = this.userService.getByUid(uid);

        if (userDTO == null) {

            throw new UserNotFoundException();
        }

        Map<String, Object> customExtensions = new HashMap<>();
        try {
            customExtensions.put("user", objectMapper.writeValueAsString(userDTO));
        } catch (JsonProcessingException e) {
            log.error("write user json failed | uid={}, user={}, e=", uid,
                    userDTO, e);
        }
        customExtensions.put("room_id", roomInfo.getRoomId());
        this.imApi.sendUserCustomMessage(userDTO.getChatUid(),
                userService.getByUid(roomInfo.getOwner()).getUid(),
                CustomEventType.INVITE_REFUSED.getValue(), customExtensions, new HashMap<>());

        return Boolean.TRUE;

    }

    @Override
    public void exchangeMic(String chatroomId, Integer from, Integer to, String uid,
            String roomId) {

        if (from == 0) {
            throw new MicStatusCannotBeModifiedException();
        }

        this.exchangeMicInfo(chatroomId, uid, from, to, roomId);

    }

    private void exchangeMicInfo(String chatroomId, String uid, Integer from, Integer to,
            String roomId) {

        String fromMicKey = buildMicKey(from);

        String toMicKey = buildMicKey(to);

        RLock micFromLock = redisson.getLock(buildMicLockKey(from, chatroomId));
        RLock micToLock = redisson.getLock(buildMicLockKey(to, chatroomId));
        boolean fromLockKey = false;
        boolean toLockKey = false;
        try {
            fromLockKey = micFromLock.tryLock(100, TimeUnit.MILLISECONDS);
            toLockKey = micToLock.tryLock(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("the thread has been interrupted!");
            throw new VoiceRoomException("400500", "update room mic info failed",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!fromLockKey || !toLockKey) {
            throw new VoiceRoomException("400403", "the room mic can not be modified!",
                    HttpStatus.FORBIDDEN);
        }
        try {

            ChatRoomMetadataGetResponse chatRoomMetadataGetResponse =
                    imApi.listChatRoomMetadata(chatroomId, Arrays.asList(fromMicKey, toMicKey));
            if (chatRoomMetadataGetResponse == null) {
                throw new VoiceRoomException("400500", "easemob service rquest failed",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, String> metadata = chatRoomMetadataGetResponse.getMetadata();

            if (metadata != null && metadata.containsKey(fromMicKey) && metadata
                    .containsKey(toMicKey)) {

                MicMetadataValue fromMicMetadataValue = null;

                MicMetadataValue toMicMetadataValue = null;

                try {
                    fromMicMetadataValue = objectMapper
                            .readValue(metadata.get(fromMicKey), MicMetadataValue.class);

                    toMicMetadataValue =
                            objectMapper.readValue(metadata.get(toMicKey), MicMetadataValue.class);

                } catch (JsonProcessingException e) {
                    log.error(
                            "parse voice room micMetadataValue json failed | chatroomId={}, uid={},"
                                    + " json={}, e=", chatroomId, uid, e);
                }

                if (fromMicMetadataValue == null || toMicMetadataValue == null) {
                    throw new MicInitException();
                }

                if (StringUtils.isEmpty(fromMicMetadataValue.getUid()) || !fromMicMetadataValue
                        .getUid().equals(uid)) {
                    throw new MicNotBelongYouException();
                }

                if (toMicMetadataValue.getStatus() != MicStatus.FREE.getStatus() && toMicMetadataValue.getStatus() != MicStatus.MUTE.getStatus()) {
                    throw new MicStatusCannotBeModifiedException();
                }


                if (toMicMetadataValue.getStatus() == MicStatus.FREE.getStatus()) {
                    toMicMetadataValue =
                            new MicMetadataValue(this.userService.getByUid(uid),
                                    MicStatus.NORMAL.getStatus(), to);
                } else {
                    toMicMetadataValue =
                            new MicMetadataValue(this.userService.getByUid(uid), toMicMetadataValue.getStatus(), to);
                }

                if (fromMicMetadataValue.getStatus() == MicStatus.MUTE.getStatus()) {
                    fromMicMetadataValue =
                            new MicMetadataValue(null, fromMicMetadataValue.getStatus(), from);
                } else {
                    fromMicMetadataValue =
                            new MicMetadataValue(null, MicStatus.FREE.getStatus(), from);
                }


                metadata = new HashMap<>();
                try {
                    metadata.put(fromMicKey, objectMapper.writeValueAsString(fromMicMetadataValue));
                    metadata.put(toMicKey, objectMapper.writeValueAsString(toMicMetadataValue));
                } catch (Exception e) {
                    log.error("parse json error", e);
                }

                imApi.setChatRoomMetadata(OPERATOR, chatroomId, metadata, AutoDelete.DELETE);

                this.voiceRoomUserService
                        .updateVoiceRoomUserMicIndex(roomId, toMicMetadataValue.getUid(), to);

            } else {
                throw new MicInitException();
            }

        } finally {
            if (micFromLock.isLocked() && micFromLock.isHeldByCurrentThread()) {
                micFromLock.unlock();
            }

            if (micToLock.isLocked() && micToLock.isHeldByCurrentThread()) {
                micToLock.unlock();
            }
        }

    }

    private void updateVoiceRoomMicInfo(String chatroomId, UserDTO user, Integer micIndex,
            Integer micOperateStatus, Boolean isAdminOperate, String roomId) {
        Instant now = Instant.now();
        String metadataKey = buildMicKey(micIndex);
        String redisLockKey = buildMicLockKey(micIndex, chatroomId);

        RLock micLock = redisson.getLock(redisLockKey);
        boolean locked = micLock.isLocked();
        if (locked) {
            Duration delay = Duration.between(now, Instant.now());
            registry.timer("voice.room.mic.lock", "result", "isLocked").record(delay);
            throw new VoiceRoomException("400403", "the room mic can not be modified!",
                    HttpStatus.FORBIDDEN);
        }

        try {
            boolean lock = micLock.tryLock(100, TimeUnit.MILLISECONDS);
            if (!lock) {
                Duration delay = Duration.between(now, Instant.now());
                registry.timer("voice.room.mic.lock", "result", "failed").record(delay);
                throw new VoiceRoomException("400403", "the room mic can not be modified!",
                        HttpStatus.FORBIDDEN);
            }
        } catch (InterruptedException e) {
            log.error("the thread has been interrupted!");
            Duration delay = Duration.between(now, Instant.now());
            registry.timer("voice.room.mic.lock", "result", "interrupted").record(delay);
            throw new VoiceRoomException("400500", "update room mic info failed",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Duration delay = Duration.between(now, Instant.now());
        registry.timer("voice.room.mic.lock", "result", "success").record(delay);
        try {
            Instant getStartTimeStamp = Instant.now();
            Map<String, String> metadata;
            ChatRoomMetadataGetResponse chatRoomMetadataGetResponse =
                    imApi.listChatRoomMetadata(chatroomId, Arrays.asList(metadataKey));
            if (chatRoomMetadataGetResponse == null) {
                throw new VoiceRoomException("400500", "easemob service rquest failed",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            metadata = chatRoomMetadataGetResponse.getMetadata();
            registry.timer("voice.room.mic.metadata", "operate", "get")
                    .record(Duration.between(getStartTimeStamp, Instant.now()));
            if (metadata != null && metadata.containsKey(metadataKey)) {

                MicMetadataValue micMetadataValue = null;

                try {
                    micMetadataValue = objectMapper
                            .readValue(metadata.get(metadataKey), MicMetadataValue.class);

                } catch (JsonProcessingException e) {
                    log.error(
                            "parse voice room micMetadataValue json failed ", e);
                }

                Integer updateStatus = null;
                String updateUid = micMetadataValue.getUid();

                String roomUserUid = null;
                Integer roomUsermicIndex = -1;

                String uid = null;

                if (user != null) {
                    uid = user.getUid();
                }

                if (!Boolean.TRUE.equals(isAdminOperate) && !StringUtils
                        .isEmpty(micMetadataValue.getUid())
                        && !micMetadataValue.getUid()
                        .equals(uid)) {
                    throw new MicNotBelongYouException();
                }

                switch (MicOperateStatus.parse(micOperateStatus)) {
                    case UP_MIC:
                        if (micMetadataValue.getStatus() == MicStatus.FREE.getStatus() || (
                                micMetadataValue.getStatus() == MicStatus.MUTE.getStatus()
                                        && StringUtils.isEmpty(micMetadataValue.getUid()))) {
                            if (micMetadataValue.getStatus() == MicStatus.FREE.getStatus()) {
                                updateStatus = MicStatus.NORMAL.getStatus();
                            } else {
                                updateStatus = MicStatus.MUTE.getStatus();
                            }
                            updateUid = uid;
                            roomUserUid = uid;
                            roomUsermicIndex = micIndex;
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case OPEN_MIC:
                        if (micMetadataValue.getStatus() == MicStatus.CLOSE.getStatus()) {
                            updateStatus = MicStatus.NORMAL.getStatus();
                            updateUid = uid;
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case CLOSE_MIC:
                        if (micMetadataValue.getStatus() == MicStatus.NORMAL.getStatus()) {
                            updateStatus = MicStatus.CLOSE.getStatus();
                            updateUid = uid;
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case LEAVE_MIC:
                        if (micIndex == 0) {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        updateStatus = MicStatus.FREE.getStatus();
                        updateUid = null;
                        roomUserUid = uid;
                        break;
                    case MUTE_MIC:
                        if (micIndex == 0) {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        if (Boolean.TRUE.equals(isAdminOperate)
                                && micMetadataValue.getStatus() != MicStatus.MUTE.getStatus()
                                && micMetadataValue.getStatus() != MicStatus.LOCK_AND_MUTE
                                .getStatus()) {
                            updateStatus = MicStatus.MUTE.getStatus();
                            if (micMetadataValue.getStatus() == MicStatus.LOCK.getStatus()) {
                                updateStatus = MicStatus.LOCK_AND_MUTE.getStatus();
                            }
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case UNMUTE_MIC:
                        if (Boolean.TRUE.equals(isAdminOperate)
                                && (micMetadataValue.getStatus() == MicStatus.MUTE
                                .getStatus()
                                || micMetadataValue.getStatus() == MicStatus.LOCK_AND_MUTE
                                .getStatus())) {
                            updateStatus = MicStatus.NORMAL.getStatus();
                            if (StringUtils.isEmpty(micMetadataValue.getUid())) {
                                updateStatus = MicStatus.FREE.getStatus();
                            }
                            if (micMetadataValue.getStatus() == MicStatus.LOCK_AND_MUTE
                                    .getStatus()) {
                                updateStatus = MicStatus.LOCK.getStatus();
                            }
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case LOCK_MIC:
                        if (micIndex == 0) {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        if (Boolean.TRUE.equals(isAdminOperate)
                                && micMetadataValue.getStatus() != MicStatus.LOCK
                                .getStatus()
                                && micMetadataValue.getStatus() != MicStatus.LOCK_AND_MUTE
                                .getStatus()) {
                            updateStatus = MicStatus.LOCK.getStatus();
                            if (micMetadataValue.getStatus() == MicStatus.MUTE.getStatus()) {
                                updateStatus = MicStatus.LOCK_AND_MUTE.getStatus();
                            }
                            updateUid = null;
                            roomUserUid = micMetadataValue.getUid();
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case UNLOCK_MIC:
                        if (Boolean.TRUE.equals(isAdminOperate)
                                && (micMetadataValue.getStatus() == MicStatus.LOCK
                                .getStatus()
                                || micMetadataValue.getStatus() == MicStatus.LOCK_AND_MUTE
                                .getStatus())) {
                            updateStatus = MicStatus.FREE.getStatus();
                            if (micMetadataValue.getStatus() == MicStatus.LOCK_AND_MUTE
                                    .getStatus()) {
                                updateStatus = MicStatus.MUTE.getStatus();
                            }
                            updateUid = null;
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    case KICK_MIC:
                        if (micIndex == 0) {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        if (Boolean.TRUE.equals(isAdminOperate) && !StringUtils
                                .isEmpty(micMetadataValue.getUid())) {
                            updateStatus = MicStatus.FREE.getStatus();
                            updateUid = null;
                            roomUserUid = micMetadataValue.getUid();
                        } else {
                            throw new MicStatusCannotBeModifiedException();
                        }
                        break;
                    default:
                        break;
                }

                //更新麦位信息
                Instant updateStartTimeStamp = Instant.now();

                if (updateUid == null) {
                    micMetadataValue =
                            new MicMetadataValue(null, updateStatus, micIndex);
                } else if (updateUid.equals(micMetadataValue.getUid())) {
                    micMetadataValue =
                            new MicMetadataValue(micMetadataValue.getMember(), updateStatus,
                                    micIndex);
                } else {
                    micMetadataValue =
                            new MicMetadataValue(user, updateStatus, micIndex);
                }

                metadata = new HashMap<>();
                try {
                    metadata.put(metadataKey, objectMapper.writeValueAsString(micMetadataValue));
                } catch (Exception e) {
                    log.error("parse json error", e);
                }
                imApi.setChatRoomMetadata(OPERATOR, chatroomId, metadata, AutoDelete.DELETE);
                registry.timer("voice.room.mic.metadata", "operate", "set")
                        .record(Duration.between(updateStartTimeStamp, Instant.now()));
                if (!StringUtils.isEmpty(roomUserUid)) {
                    Instant updateUserStartTimeStamp = Instant.now();
                    this.voiceRoomUserService
                            .updateVoiceRoomUserMicIndex(roomId, roomUserUid, roomUsermicIndex);
                    registry.timer("voice.room.user", "operate", "update")
                            .record(Duration.between(updateUserStartTimeStamp, Instant.now()));
                }

            } else {
                throw new MicInitException();
            }

        } finally {
            if (micLock.isLocked() && micLock.isHeldByCurrentThread()) {
                micLock.unlock();
            }
            registry.timer("voice.room.mic", "operate", "mute")
                    .record(Duration.between(now, Instant.now()));
        }

    }

    private List<MicInfo> buildMicInfo(Map<String, String> metadata) {
        List<MicInfo> micInfos = new ArrayList<>();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            MicMetadataValue micMetadataValue = null;

            try {
                micMetadataValue = objectMapper
                        .readValue(value, MicMetadataValue.class);

            } catch (JsonProcessingException e) {
                log.error(
                        "parse voice room micMetadataValue json failed ", e);
            }

            UserDTO user = micMetadataValue.getMember();

            int index = -1;
            try {
                index = Integer.parseInt(key.split("_")[1]);
            } catch (Exception e) {
                log.error(
                        "mic index less than zero,index:{}", index, e);
            }
            if (index < 0) {
                throw new MicInitException();
            }
            MicInfo micInfo =
                    MicInfo.builder().status(micMetadataValue.getStatus()).micIndex(index)
                            .member(user)
                            .build();
            micInfos.add(micInfo);

        }
        micInfos = micInfos.stream().sorted(Comparator.comparing(MicInfo::getMicIndex)).collect(
                Collectors.toList());
        return micInfos;
    }

    private String buildMicKey(Integer micIndex) {
        return METADATA_PREFIX_KEY + "_" + micIndex;
    }

    private String buildMicLockKey(Integer micIndex, String chatRoomId) {
        return chatRoomId + "_" + buildMicKey(micIndex) + "_lock";
    }

    private MicMetadataValue buildMicMetadataValue(String chatroomId, Integer micIndex) {

        String metadataKey = buildMicKey(micIndex);

        Map<String, String> metadata = null;
        ChatRoomMetadataGetResponse chatRoomMetadataGetResponse =
                imApi.listChatRoomMetadata(chatroomId, Arrays.asList(metadataKey));
        if (chatRoomMetadataGetResponse == null) {
            throw new VoiceRoomException("400500", "easemob service rquest failed",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        metadata = chatRoomMetadataGetResponse.getMetadata();
        if (metadata != null && metadata.containsKey(metadataKey)) {

            MicMetadataValue micMetadataValue = null;

            try {
                micMetadataValue = objectMapper
                        .readValue(metadata.get(metadataKey), MicMetadataValue.class);

            } catch (JsonProcessingException e) {
                log.error(
                        "parse voice room micMetadataValue json failed | chatroomId={}, micIndex={}",
                        chatroomId, micIndex, e);
            }

            if (micMetadataValue == null) {
                throw new MicInitException();
            }
            return micMetadataValue;
        } else {
            throw new MicInitException();
        }
    }
}
