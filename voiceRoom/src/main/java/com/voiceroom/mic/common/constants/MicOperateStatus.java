package com.voiceroom.mic.common.constants;

/**
 * Created by liyongxin on 2022/8/31
 */

public enum MicOperateStatus {

    UP_MIC(0, "上麦"),
    OPEN_MIC(1, "开麦"),
    CLOSE_MIC(2, "闭麦"),
    LEAVE_MIC(3, "下麦"),
    MUTE_MIC(4, "禁言麦位"),
    UNMUTE_MIC(5, "取消禁言"),
    KICK_MIC(6, "踢人下麦"),
    LOCK_MIC(7, "锁麦"),
    UNLOCK_MIC(8, "取消锁麦");

    private Integer status;
    private String desc;

    MicOperateStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return this.status;
    }

    public static MicOperateStatus parse(Integer status) {
        if (status == null) {
            return null;
        }
        for (MicOperateStatus mic : values()) {
            if (status == mic.getStatus()) {
                return mic;
            }
        }
        return null;
    }

}
