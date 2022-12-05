package com.voiceroom.mic.common.constants;

import org.apache.commons.lang.StringUtils;

/**
 * Created by liyongxin on 2022/8/31
 */

public enum CustomEventType {

    /**
     * 申请上麦
     */
    APPLY_SITE("chatroom_applySiteNotify"),

    /**
     * 取消申请上麦
     */
    APPLY_CANCEL("chatroom_cancelApplySiteNotify"),

    /**
     * 拒绝申请
     */
    APPLY_REFUSED("chatroom_applyRefusedNotify"),
    /**
     * 邀请上麦
     */
    INVITE_SITE("chatroom_inviteSiteNotify"),
    /**
     * 拒绝邀请
     */
    INVITE_REFUSED("chatroom_inviteRefusedNotify"),

    /**
     * 送礼物
     */
    SEND_GIFT("chatroom_gift"),

    /**
     * 调节机器人音量
     */
    UPDATE_ROBOT_VOLUME("chatroom_updateRobotVolume"),
    /**
     * 加入语聊房
     */
    JOIN_VOICE_ROOM("chatroom_join"),

    LEAVE_VOICE_ROOM("chatroom_leave"),
    ;

    private String eventType;

    CustomEventType(String type) {
        this.eventType = type;
    }

    @Override
    public String toString() {
        return this.eventType;
    }

    public String getValue() {
        return this.eventType;
    }

    public static CustomEventType parse(String eventType) {
        if (StringUtils.isEmpty(eventType)) {
            return null;
        }
        for (CustomEventType type : values()) {
            if (type.toString().equalsIgnoreCase(eventType)) {
                return type;
            }
        }
        return null;
    }

}
