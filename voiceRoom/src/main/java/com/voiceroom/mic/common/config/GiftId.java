package com.voiceroom.mic.common.config;

import com.voiceroom.mic.exception.GiftNotFoundException;

public enum GiftId {

    VoiceRoomGift1(1L),
    VoiceRoomGift2(5L),
    VoiceRoomGift3(10L),
    VoiceRoomGift4(20L),
    VoiceRoomGift5(50L),
    VoiceRoomGift6(100L),
    VoiceRoomGift7(500L),
    VoiceRoomGift8(1000L),
    VoiceRoomGift9(1500L),
    VoiceRoomGift10(10000L),

    ;

    private final Long amount;

    GiftId(Long amount) {
        this.amount = amount;
    }

    public Long getAmount() {
        return amount;
    }

    public static GiftId of(String giftId) {
        switch (giftId) {
            case "VoiceRoomGift1":
                return VoiceRoomGift1;
            case "VoiceRoomGift2":
                return VoiceRoomGift2;
            case "VoiceRoomGift3":
                return VoiceRoomGift3;
            case "VoiceRoomGift4":
                return VoiceRoomGift4;
            case "VoiceRoomGift5":
                return VoiceRoomGift5;
            case "VoiceRoomGift6":
                return VoiceRoomGift6;
            case "VoiceRoomGift7":
                return VoiceRoomGift7;
            case "VoiceRoomGift8":
                return VoiceRoomGift8;
            case "VoiceRoomGift9":
                return VoiceRoomGift9;
            case "VoiceRoomGift10":
                return VoiceRoomGift10;
            default:
                throw new GiftNotFoundException("not found the gift_id from server");
        }
    }
}
