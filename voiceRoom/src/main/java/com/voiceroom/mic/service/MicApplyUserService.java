package com.voiceroom.mic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.voiceroom.mic.model.MicApplyUser;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.pojos.PageInfo;
import com.voiceroom.mic.pojos.UserDTO;
import com.voiceroom.mic.pojos.vo.MicApplyVO;

public interface MicApplyUserService extends IService<MicApplyUser> {

    Boolean addMicApply(UserDTO user, VoiceRoom roomInfo, Integer micIndex);

    void deleteMicApply(String uid, VoiceRoom roomInfo, Boolean sendNotify);

    Boolean agreeApply(VoiceRoom roomInfo, String uid);

    PageInfo<MicApplyVO> getByPage(String roomId, String cursor, Integer limit);

    Boolean refuseApply(VoiceRoom roomInfo, String uid, Integer micIndex);

    /**
     * 删除房间麦位申请记录
     * @param roomId
     */
    void deleteByRoomId(String roomId);
}
