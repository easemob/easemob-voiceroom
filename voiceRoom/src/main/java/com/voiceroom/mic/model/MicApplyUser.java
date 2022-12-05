package com.voiceroom.mic.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@TableName("mic_apply_user")
public class MicApplyUser {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private String roomId;

    private String uid;

    private Integer micIndex;
}
