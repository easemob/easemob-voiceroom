package com.voiceroom.mic.pojos;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class ExchangeMicRequest {

    @NotNull(message = "exchange from mic index must not be null")
    private Integer from;

    @NotNull(message = "exchange to mic index must not be null")
    private Integer to;
}
