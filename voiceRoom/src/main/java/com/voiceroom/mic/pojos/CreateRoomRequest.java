package com.voiceroom.mic.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class CreateRoomRequest {

    @NotBlank(message = "name not allow empty")
    private String name;

    @NotNull(message = "is_private must not be null")
    @JsonProperty("is_private")
    private Boolean isPrivate;

    private String password;

    @NotNull(message = "type must not be null")
    private Integer type;

    @NotNull(message = "allow_free_join_mic must not be null")
    @JsonProperty("allow_free_join_mic")
    private Boolean allowFreeJoinMic;

    @NotBlank(message = "sound_effect not allow empty")
    @JsonProperty("sound_effect")
    private String soundEffect;

}
