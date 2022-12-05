package com.voiceroom.mic.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@TableName("gift_record")
public class GiftRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private String roomId;

    private String uid;

    private String toUid;

    private Long amount;

    public static GiftRecord create(String roomId, String uid, String toUid, Long amount) {
        return GiftRecord.builder().roomId(roomId)
                .uid(uid).toUid(toUid).amount(amount)
                .build();
    }

    public GiftRecord addAmount(Long amount) {
        amount = this.amount + amount;
        return GiftRecord.builder()
                .id(id)
                .createdAt(createdAt)
                .roomId(roomId)
                .uid(uid)
                .toUid(toUid)
                .amount(amount)
                .build();
    }

}
