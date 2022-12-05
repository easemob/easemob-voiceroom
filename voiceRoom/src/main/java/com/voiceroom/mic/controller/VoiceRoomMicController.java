package com.voiceroom.mic.controller;

import com.voiceroom.common.util.ValidationUtil;
import com.voiceroom.mic.exception.RoomNotFoundException;
import com.voiceroom.mic.exception.UserNotFoundException;
import com.voiceroom.mic.exception.UserNotInRoomException;
import com.voiceroom.mic.exception.VoiceRoomSecurityException;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.model.VoiceRoomUser;
import com.voiceroom.mic.pojos.*;
import com.voiceroom.mic.pojos.vo.MicApplyVO;
import com.voiceroom.mic.service.MicApplyUserService;
import com.voiceroom.mic.service.VoiceRoomMicService;
import com.voiceroom.mic.service.VoiceRoomService;
import com.voiceroom.mic.service.VoiceRoomUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
public class VoiceRoomMicController {

    @Resource
    private MicApplyUserService micApplyUserService;

    @Resource
    private VoiceRoomMicService voiceRoomMicService;

    @Resource
    private VoiceRoomService voiceRoomService;

    @Resource
    private VoiceRoomUserService voiceRoomUserService;

    @GetMapping("/voice/room/{roomId}/mic/apply")
    public GetMicApplyListResponse getMicApplyList(
            @RequestAttribute(name = "user", required = false) UserDTO user,
            @PathVariable("roomId") String roomId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {
        if (limit > 100) {
            throw new IllegalArgumentException("exceeded maximum paging limit");
        }
        validateMicPermissions(roomId, user.getUid());
        PageInfo<MicApplyVO> pageInfo = micApplyUserService.getByPage(roomId, cursor, limit);
        return new GetMicApplyListResponse(pageInfo.getTotal(), pageInfo.getCursor(),
                pageInfo.getList());
    }

    @PostMapping("/voice/room/{roomId}/mic/apply")
    public AddMicApplyResponse addMicApply(@PathVariable("roomId") String roomId,
            @RequestBody(required = false) @Validated AddMicApplyRequest request,
            BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = validateMicPermissions(roomId, user.getUid());
        if (user.getUid().equals(roomInfo.getOwner())) {
            throw new VoiceRoomSecurityException("admin can not apply mic");
        }
        Boolean result =
                micApplyUserService.addMicApply(user, roomInfo,
                        request == null ? null : request.getMicIndex());
        return new AddMicApplyResponse(result);
    }

    @DeleteMapping("/voice/room/{roomId}/mic/apply")
    public DeleteMicApplyResponse deleteMicApply(@PathVariable("roomId") String roomId,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = validateMicPermissions(roomId, user.getUid());
        micApplyUserService.deleteMicApply(user.getUid(), roomInfo, Boolean.TRUE);
        return new DeleteMicApplyResponse(Boolean.TRUE);
    }

    @GetMapping("/voice/room/{roomId}/mic")
    public List<MicInfo> getRoomMicInfo(
            @RequestAttribute(name = "user", required = false) UserDTO user,
            @PathVariable("roomId") String roomId) {
        VoiceRoom roomInfo = validateMicPermissions(roomId, user.getUid());
        return voiceRoomMicService.getRoomMicInfo(roomInfo);
    }

    @PostMapping("/voice/room/{roomId}/mic/close")
    public CloseMicResponse closeMic(@PathVariable("roomId") String roomId,
            @RequestBody @Validated CloseMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom voiceRoom = validateMicPermissions(roomId, user.getUid());
        this.voiceRoomMicService.closeMic(user, voiceRoom.getChatroomId(),
                request.getMicIndex(), voiceRoom.getRoomId());
        return new CloseMicResponse(Boolean.TRUE);
    }

    @DeleteMapping("/voice/room/{roomId}/mic/close")
    public OpenMicResponse openMic(@PathVariable("roomId") String roomId,
            @RequestParam("mic_index") Integer micIndex,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = validateMicPermissions(roomId, user.getUid());
        this.voiceRoomMicService.openMic(user, roomInfo.getChatroomId(),
                micIndex, roomInfo.getRoomId());
        return new OpenMicResponse(Boolean.TRUE);
    }

    @DeleteMapping("/voice/room/{roomId}/mic/leave")
    public LeaveMicResponse leaveMic(@PathVariable("roomId") String roomId,
            @RequestParam("mic_index") Integer micIndex,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = validateMicPermissions(roomId, user.getUid());
        this.voiceRoomMicService.leaveMic(user, roomInfo.getChatroomId(),
                micIndex, roomInfo.getRoomId());
        return new LeaveMicResponse(Boolean.TRUE);
    }

    @PostMapping("/voice/room/{roomId}/mic/mute")
    public MuteMicResponse muteMic(@PathVariable("roomId") String roomId,
            @RequestBody @Validated MuteMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        MuteMicResponse response = new MuteMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new VoiceRoomSecurityException("only the owner can operate");
        }
        this.voiceRoomMicService
                .muteMic(roomInfo.getChatroomId(), request.getMicIndex(), roomInfo.getRoomId());

        return response;
    }

    @DeleteMapping("/voice/room/{roomId}/mic/mute")
    public UnMuteMicResponse unMuteMic(@PathVariable("roomId") String roomId,
            @RequestParam("mic_index") Integer micIndex,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        UnMuteMicResponse response = new UnMuteMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new VoiceRoomSecurityException("only the owner can operate");
        }
        this.voiceRoomMicService
                .unMuteMic(roomInfo.getChatroomId(), micIndex, roomInfo.getRoomId());

        return response;
    }

    @PostMapping("/voice/room/{roomId}/mic/exchange")
    public ExchangeMicResponse exchangeMic(@PathVariable("roomId") String roomId,
            @RequestBody @Validated ExchangeMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        ExchangeMicResponse response = new ExchangeMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = validateMicPermissions(roomId, user.getUid());
        this.voiceRoomMicService.exchangeMic(roomInfo.getChatroomId(),
                request.getFrom(), request.getTo(), user.getUid(), roomInfo.getRoomId());
        return response;
    }

    @PostMapping("/voice/room/{roomId}/mic/kick")
    public KickUserMicResponse kickUserMic(
            @PathVariable("roomId") String roomId,
            @RequestBody @Validated KickUserMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        KickUserMicResponse response = new KickUserMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new IllegalArgumentException("only the admin can kick mic");
        }
        this.voiceRoomMicService.kickUserMic(roomInfo, request.getMicIndex(),
                request.getUid(), roomInfo.getRoomId());

        return response;
    }

    @PostMapping("/voice/room/{roomId}/mic/lock")
    public LockMicResponse lockMic(@PathVariable("roomId") String roomId,
            @RequestBody @Validated LockMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        LockMicResponse response = new LockMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new IllegalArgumentException("only the admin can lock mic");
        }
        this.voiceRoomMicService
                .lockMic(roomInfo.getChatroomId(), request.getMicIndex(), roomInfo.getRoomId());
        return response;
    }

    @DeleteMapping("/voice/room/{roomId}/mic/lock")
    public UnLockMicResponse unLockMic(@PathVariable("roomId") String roomId,
            @RequestParam("mic_index") Integer micIndex,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        UnLockMicResponse response = new UnLockMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (roomInfo == null) {
            throw new RoomNotFoundException(String.format("room %s not found", roomId));
        }
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new IllegalArgumentException("only the admin can unlock mic");
        }
        this.voiceRoomMicService
                .unLockMic(roomInfo.getChatroomId(), micIndex, roomInfo.getRoomId());
        return response;
    }

    @PostMapping("/voice/room/{roomId}/mic/invite")
    public InviteUserOnMicResponse invite(@PathVariable("roomId") String roomId,
            @RequestBody @Validated InviteUserOnMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        InviteUserOnMicResponse response = new InviteUserOnMicResponse(Boolean.TRUE);
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (roomInfo == null) {
            throw new RoomNotFoundException(String.format("room %s not found", roomId));
        }
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new IllegalArgumentException("only the admin can invite");
        }
        validateMicPermissions(roomId, request.getUid());
        this.voiceRoomMicService.invite(roomInfo, request.getMicIndex(), request.getUid());
        return response;
    }

    @PostMapping("/voice/room/{roomId}/mic/apply/agree")
    public ApplyAgreeOnMicResponse agreeApply(
            @PathVariable("roomId") String roomId,
            @RequestBody @Validated ApplyAgreeOnMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }

        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (roomInfo == null) {
            throw new RoomNotFoundException(String.format("room %s not found", roomId));
        }
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new IllegalArgumentException("only the admin can invite");
        }
        Boolean result = micApplyUserService.agreeApply(roomInfo, request.getUid());
        return new ApplyAgreeOnMicResponse(Boolean.TRUE.equals(result));
    }

    @PostMapping("/voice/room/{roomId}/mic/apply/refuse")
    public ApplyAgreeOnMicResponse refuseApply(
            @PathVariable("roomId") String roomId,
            @RequestBody @Validated ApplyRefuseOnMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (roomInfo == null) {
            throw new RoomNotFoundException(String.format("room %s not found", roomId));
        }
        if (!roomInfo.getOwner().equals(user.getUid())) {
            throw new IllegalArgumentException("only the admin can invite");
        }
        Boolean result =
                micApplyUserService.refuseApply(roomInfo, request.getUid(), request.getMicIndex());
        return new ApplyAgreeOnMicResponse(Boolean.TRUE.equals(result));
    }

    @PostMapping("/voice/room/{roomId}/mic/invite/agree")
    public InviteAgreeOnMicResponse agreeInvite(
            @PathVariable("roomId") String roomId,
            @RequestBody @Validated InviteAgreeOnMicRequest request, BindingResult bindingResult,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        ValidationUtil.validate(bindingResult);
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (roomInfo == null) {
            throw new RoomNotFoundException(String.format("room %s not found", roomId));
        }
        Boolean result =
                voiceRoomMicService.agreeInvite(roomInfo, user,
                        request.getMicIndex());
        return new InviteAgreeOnMicResponse(Boolean.TRUE.equals(result));
    }

    @GetMapping("/voice/room/{roomId}/mic/invite/refuse")
    public InviteAgreeOnMicResponse refuseInvite(
            @PathVariable("roomId") String roomId,
            @RequestAttribute(name = "user", required = false) UserDTO user) {
        if (user == null) {
            throw new UserNotFoundException();
        }
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        if (roomInfo == null) {
            throw new RoomNotFoundException(String.format("room %s not found", roomId));
        }
        Boolean result =
                voiceRoomMicService.refuseInvite(roomInfo, user.getUid());
        return new InviteAgreeOnMicResponse(Boolean.TRUE.equals(result));
    }

    private VoiceRoom validateMicPermissions(String roomId, String uid) {
        VoiceRoom roomInfo = voiceRoomService.findByRoomId(roomId);
        VoiceRoomUser voiceRoomUser =
                voiceRoomUserService.findByRoomIdAndUid(roomInfo.getRoomId(), uid);
        if (!uid.equals(roomInfo.getOwner()) && voiceRoomUser == null) {
            throw new UserNotInRoomException();
        }
        return roomInfo;
    }

}
