package com.voiceroom.mic.service;

public interface SmsService {

    public String sendSmsCode(String phone);

    public boolean checkSmsCode(String phone, String code);
}
