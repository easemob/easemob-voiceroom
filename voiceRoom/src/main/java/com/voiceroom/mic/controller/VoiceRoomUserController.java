package com.voiceroom.mic.controller;

import com.voiceroom.mic.exception.UserNotFoundException;
import com.voiceroom.mic.exception.VoiceRoomSecurityException;
import com.voiceroom.mic.pojos.*;
import com.voiceroom.mic.service.VoiceRoomUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@RestController
public class VoiceRoomUserController {

    @Resource
    private VoiceRoomUserService voiceRoomUserService;

    @GetMapping("/voice/room/{roomId}/members/list")
    public GetRoomUserListResponse getRoomMemberList(@PathVariable("roomId") String roomId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {
        if (limit > 100) {
            throw new IllegalArgumentException("exceeded maximum paging limit");
        }
        PageInfo<UserDTO> pageInfo =
                voiceRoomUserService.findPageByRoomId(roomId, cursor, limit);
        if (pageInfo.getList() == null || pageInfo.getList().isEmpty()) {
            return new GetRoomUserListResponse(0L, null, Collections.emptyList());
        }
        return new GetRoomUserListResponse(pageInfo.getTotal(), pageInfo.getCursor(),
                pageInfo.getList());
    }

    @PostMapping("/voice/room/{roomId}/members/join")
    public JoinRoomResponse joinRoom(@PathVariable("roomId") String roomId,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException("join room user must not be null");
        }
        voiceRoomUserService.addVoiceRoomUser(roomId, user);
        return new JoinRoomResponse(Boolean.TRUE);
    }

    @DeleteMapping("/voice/room/{roomId}/members/leave")
    public LeaveRoomResponse leaveRoom(@PathVariable("roomId") String roomId,
            @RequestAttribute(name = "user", required = false) UserDTO user,
            @RequestParam(name = "is_success", required = false) Boolean isSuccess) {
        if (user == null) {
            throw new UserNotFoundException("leave room user must not be null");
        }
        voiceRoomUserService.deleteVoiceRoomUser(roomId, user, isSuccess);
        return new LeaveRoomResponse(Boolean.TRUE);
    }

    @DeleteMapping("/voice/room/{roomId}/members/kick")
    public KickRoomResponse kickRoom(@PathVariable("roomId") String roomId,
            @RequestParam("uid") String uid,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new VoiceRoomSecurityException("not the owner can't operate");
        }
        voiceRoomUserService.kickVoiceRoomUser(roomId, user.getUid(), uid);
        return new KickRoomResponse(Boolean.TRUE);
    }

}
