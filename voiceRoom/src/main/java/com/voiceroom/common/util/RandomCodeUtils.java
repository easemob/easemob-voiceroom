package com.voiceroom.common.util;

public class RandomCodeUtils {

    public static int smsCode() {
        return (int)((Math.random() * 9 + 1) * 100000);
    }

}
