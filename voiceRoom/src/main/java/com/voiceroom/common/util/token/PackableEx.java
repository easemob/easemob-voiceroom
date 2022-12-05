package com.voiceroom.common.util.token;


public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
