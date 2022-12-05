package com.voiceroom.mic.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceroom.common.im.ImApi;
import com.voiceroom.mic.common.constants.CustomEventType;
import com.voiceroom.mic.exception.MicApplyException;
import com.voiceroom.mic.exception.MicApplyRecordNotFoundException;
import com.voiceroom.mic.exception.MicApplyRepeatException;
import com.voiceroom.mic.exception.MicIndexNullException;
import com.voiceroom.mic.model.MicApplyUser;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.pojos.PageInfo;
import com.voiceroom.mic.pojos.UserDTO;
import com.voiceroom.mic.pojos.vo.MicApplyVO;
import com.voiceroom.mic.repository.MicApplyUserMapper;
import com.voiceroom.mic.service.MicApplyUserService;
import com.voiceroom.mic.service.UserService;
import com.voiceroom.mic.service.VoiceRoomMicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MicApplyUserServiceImpl extends ServiceImpl<MicApplyUserMapper, MicApplyUser>
        implements MicApplyUserService {

    @Value("${local.zone.offset:+8}")
    private String zoneOffset;

    @Resource
    private ImApi imApi;

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private VoiceRoomMicService voiceRoomMicService;

    @Override
    public Boolean addMicApply(UserDTO user, VoiceRoom roomInfo, Integer micIndex) {
        String roomId = roomInfo.getRoomId();
        String uid = user.getUid();
        if (!Boolean.TRUE.equals(roomInfo.getAllowedFreeJoinMic())) {
            try {

                LambdaQueryWrapper<MicApplyUser> wrapper = new LambdaQueryWrapper<MicApplyUser>()
                        .eq(MicApplyUser::getUid, uid)
                        .eq(MicApplyUser::getRoomId, roomId);
                MicApplyUser micApplyUser = this.getOne(wrapper);
                if (micApplyUser != null) {
                    throw new MicApplyRepeatException();
                }

                micApplyUser = MicApplyUser.builder()
                        .micIndex(micIndex)
                        .roomId(roomId)
                        .uid(uid)
                        .build();
                this.save(micApplyUser);

                UserDTO applyUser = userService.getByUid(uid);

                Map<String, Object> customExtensions = new HashMap<>();
                customExtensions.put("user", objectMapper.writeValueAsString(applyUser));
                customExtensions.put("mic_index", String.valueOf(micIndex));
                customExtensions.put("room_id", roomInfo.getRoomId());
                this.imApi
                        .sendUserCustomMessage(applyUser.getChatUid(), userService.getByUid(roomInfo.getOwner()).getChatUid(),
                                CustomEventType.APPLY_SITE.getValue(), customExtensions,
                                new HashMap<>());
                return Boolean.TRUE;
            } catch (Exception e) {
                log.error("addMicApply error,uid:{},roomId:{}", uid, roomId, e);
                if (e instanceof SQLIntegrityConstraintViolationException
                        || e instanceof MicApplyRepeatException) {
                    throw new MicApplyRepeatException();
                }
                throw new MicApplyException();
            }
        } else {
            if (micIndex == null) {
                throw new MicIndexNullException();
            }
            return this.voiceRoomMicService.setRoomMicInfo(roomInfo, user, micIndex,
                    Boolean.FALSE);
        }

    }

    @Override
    public void deleteMicApply(String uid, VoiceRoom roomInfo, Boolean sendNotify) {
        LambdaQueryWrapper<MicApplyUser> wrapper = new LambdaQueryWrapper<MicApplyUser>()
                .eq(MicApplyUser::getUid, uid)
                .eq(MicApplyUser::getRoomId, roomInfo.getRoomId());
        int count = this.baseMapper.delete(wrapper);
        if (count == 0) {
            return;
        }
        UserDTO applyUser = userService.getByUid(uid);
        if (Boolean.TRUE.equals(sendNotify)) {
            Map<String, Object> customExtensions = new HashMap<>();
            try {
                customExtensions.put("user", objectMapper.writeValueAsString(applyUser));
            } catch (JsonProcessingException e) {
                log.error("write user json failed | uid={}, user={}, e=", uid,
                        applyUser, e);
            }
            customExtensions.put("room_id", roomInfo.getRoomId());
            this.imApi.sendUserCustomMessage(applyUser.getChatUid(), userService.getByUid(roomInfo.getOwner()).getChatUid(),
                    CustomEventType.APPLY_CANCEL.getValue(), customExtensions, new HashMap<>());
        }

    }

    @Override
    public Boolean agreeApply(VoiceRoom roomInfo, String uid) {
        String roomId = roomInfo.getRoomId();
        LambdaQueryWrapper<MicApplyUser> wrapper = new LambdaQueryWrapper<MicApplyUser>()
                .eq(MicApplyUser::getUid, uid)
                .eq(MicApplyUser::getRoomId, roomId);
        MicApplyUser micApplyUser = this.getOne(wrapper);
        if (micApplyUser == null) {
            throw new MicApplyRecordNotFoundException();
        }
        Integer micIndex = micApplyUser.getMicIndex();
        Boolean result =
                voiceRoomMicService.setRoomMicInfo(roomInfo, userService.getByUid(uid), micIndex,
                        Boolean.TRUE);
        deleteMicApply(uid, roomInfo, Boolean.FALSE);
        return result;

    }

    @Override
    public Boolean refuseApply(VoiceRoom roomInfo, String uid, Integer micIndex) {

        deleteMicApply(uid, roomInfo, Boolean.FALSE);

        UserDTO applyUser = this.userService.getByUid(uid);
        UserDTO ownerUser = this.userService.getByUid(roomInfo.getOwner());

        Map<String, Object> customExtensions = new HashMap<>();
        customExtensions.put("user", JSONObject.toJSONString(applyUser));
        if (micIndex != null) {
            customExtensions.put("mic_index", micIndex.toString());
        }
        customExtensions.put("room_id", roomInfo.getRoomId());
        this.imApi.sendUserCustomMessage(ownerUser.getChatUid(), applyUser.getChatUid(),
                CustomEventType.APPLY_REFUSED.getValue(), customExtensions, new HashMap<>());
        return Boolean.TRUE;
    }

    @Override public void deleteByRoomId(String roomId) {
        LambdaQueryWrapper<MicApplyUser> queryWrapper =
                new LambdaQueryWrapper<MicApplyUser>().eq(MicApplyUser::getRoomId, roomId);
        baseMapper.delete(queryWrapper);
    }

    @Override public PageInfo<MicApplyVO> getByPage(String roomId, String cursor, Integer limit) {
        List<MicApplyUser> micApplyUser;
        int limitSize = limit + 1;
        Long total = baseMapper.selectCount(
                new LambdaQueryWrapper<MicApplyUser>().eq(MicApplyUser::getRoomId, roomId));
        if (StringUtils.isBlank(cursor)) {
            LambdaQueryWrapper<MicApplyUser> queryWrapper =
                    new LambdaQueryWrapper<MicApplyUser>()
                            .eq(MicApplyUser::getRoomId, roomId)
                            .orderByDesc(MicApplyUser::getId)
                            .last(" limit " + limitSize);
            micApplyUser = baseMapper.selectList(queryWrapper);
        } else {
            String s = new String(
                    Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8);
            int id = Integer.parseInt(s);
            LambdaQueryWrapper<MicApplyUser> queryWrapper =
                    new LambdaQueryWrapper<MicApplyUser>()
                            .eq(MicApplyUser::getRoomId, roomId)
                            .le(MicApplyUser::getId, id)
                            .orderByDesc(MicApplyUser::getId)
                            .last(" limit " + limitSize);
            micApplyUser = baseMapper.selectList(queryWrapper);
        }
        if (micApplyUser.size() == limitSize) {
            MicApplyUser micApply = micApplyUser.get(limitSize - 1);
            Integer id = micApply.getId();
            cursor = Base64.getUrlEncoder()
                    .encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
            micApplyUser.remove(micApply);
        } else {
            cursor = null;
        }
        List<String> ownerUidList =
                micApplyUser.stream().map(MicApplyUser::getUid).collect(Collectors.toList());
        Map<String, UserDTO> ownerMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(ownerUidList)) {
            ownerMap = userService.findByUidList(ownerUidList);
        }

        List<MicApplyVO> list = new ArrayList<>();
        for (MicApplyUser applyUser : micApplyUser) {
            UserDTO userDTO = ownerMap.get(applyUser.getUid());
            long createdAt = applyUser.getCreatedAt().toInstant(ZoneOffset.of(zoneOffset))
                    .toEpochMilli();
            list.add(MicApplyVO.builder().member(userDTO).micIndex(applyUser.getMicIndex())
                    .createdAt(createdAt).build());
        }
        PageInfo<MicApplyVO> pageInfo = new PageInfo<>();
        pageInfo.setCursor(cursor);
        pageInfo.setTotal(total);
        pageInfo.setList(list);
        return pageInfo;
    }
}
