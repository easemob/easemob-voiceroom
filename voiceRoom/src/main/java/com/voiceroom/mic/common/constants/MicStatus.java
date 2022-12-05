package com.voiceroom.mic.common.constants;

/**
 * Created by liyongxin on 2022/8/31
 */

public enum MicStatus {

    INACTIVE(-2,"未激活"),
    FREE(-1, "空闲"),
    NORMAL(0, "正常"),
    CLOSE(1, "闭麦"),
    MUTE(2, "禁言"),
    LOCK(3, "锁麦"),
    LOCK_AND_MUTE(4, "锁麦和禁言"),
    ACTIVE(5, "激活");

    private Integer status;
    private String desc;

    MicStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return this.status;
    }

    public static MicStatus parse(Integer status) {
        if (status == null) {
            return null;
        }
        for (MicStatus mic : values()) {
            if (status == mic.getStatus()) {
                return mic;
            }
        }
        return null;
    }

}
