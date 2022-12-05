package com.voiceroom.mic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.pojos.*;

public interface VoiceRoomService extends IService<VoiceRoom> {

    /**
     * 新建房间
     *
     * @param owner
     * @param request
     * @return
     */
    VoiceRoom create(UserDTO owner, CreateRoomRequest request);

    /**
     * 分页获取房间信息
     *
     * @param cursor
     * @param limit
     * @param type
     * @return
     */
    PageInfo<RoomListDTO> getByPage(String cursor, int limit, Integer type);

    /**
     * 根据房间id获取房间信息
     *
     * @param roomId
     * @return
     */
    VoiceRoom findByRoomId(String roomId);

    /**
     * 根据房间id修改房间信息
     *
     * @param roomId
     * @param request
     * @param owner
     */
    void updateByRoomId(String roomId, UpdateRoomInfoRequest request, String owner);

    /**
     * 根据房间id删除房间
     *
     * @param roomId
     * @param owner
     */
    void deleteByRoomId(String roomId, String owner);

    /**
     * 获取房间点击数
     *
     * @param roomId
     * @return
     */
    public Long getClickCount(String roomId);

    /**
     * 获取房间成员数
     *
     * @param roomId
     * @return
     */
    public Long getMemberCount(String roomId);

    /**
     * 验证密码
     *
     * @param roomId
     * @param password
     * @return
     */
    public Boolean validPassword(String roomId, String password);
}
