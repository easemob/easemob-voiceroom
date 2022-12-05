package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voiceroom.mic.pojos.vo.GiftRecordVO;
import lombok.Value;

import java.util.List;

@Value
public class GetGiftListResponse {

    @JsonProperty("ranking_list")
    private List<GiftRecordVO> rankingList;

    @JsonProperty("gift_amount")
    private Long giftAmount;

}
