package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.util.List;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRoomListResponse {

    private Long total;

    private String cursor;

    private List<RoomListDTO> rooms;
}
