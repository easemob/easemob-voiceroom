package com.voiceroom.mic.controller;

import com.voiceroom.common.util.PhoneUtils;
import com.voiceroom.common.util.ValidationUtil;
import com.voiceroom.common.util.token.TokenProvider;
import com.voiceroom.mic.common.jwt.util.VoiceRoomJwtUtil;
import com.voiceroom.mic.pojos.*;
import com.voiceroom.mic.service.SmsService;
import com.voiceroom.mic.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private VoiceRoomJwtUtil voiceRoomJwtUtil;

    @Resource
    private TokenProvider tokenProvider;

    @Resource
    private SmsService smsService;

    @Value("${voice.room.login.auth.phone:false}")
    private Boolean isAuthPhone;

    @PostMapping(value = "/login/device")
    public LoginResponse loginDevice(@RequestBody @Validated LoginRequest request,
            BindingResult result) {
        ValidationUtil.validate(result);
        if (StringUtils.isBlank(request.getPhone()) || StringUtils.isBlank(
                request.getVerifyCode())) {
            if (isAuthPhone) {
                throw new IllegalArgumentException("login request the phone number must not be empty");
            }
        } else {
            String phone = request.getPhone();
            PhoneUtils.isPhoneNumber(phone);
            String code = request.getVerifyCode();
            boolean checkResult = smsService.checkSmsCode(phone, code);
            if (!checkResult) {
                throw new IllegalArgumentException(
                        "login request the phone number and code check failed");
            }
        }
        UserDTO userDTO = userService.loginDevice(request.getDeviceId(), request.getName(),
                request.getPortrait());
        String jwtToken = voiceRoomJwtUtil.createJWT(userDTO.getUid());
        String imToken = tokenProvider.buildImToken(userDTO);
        return new LoginResponse(userDTO.getUid(), userDTO.getName(), userDTO.getPortrait(),
                userDTO.getChatUid(), jwtToken, imToken, userDTO.getRtcUid());
    }

    @PostMapping("/sms/send")
    public SendSmsResponse sendSms(@RequestBody @Valid SendSmsRequest sendSmsRequest) {
        String phoneNumber = sendSmsRequest.getPhoneNumber();

        PhoneUtils.isPhoneNumber(phoneNumber);

        String code = smsService.sendSmsCode(phoneNumber);
        if (StringUtils.isBlank(code)) {
            return new SendSmsResponse(false);
        }
        return new SendSmsResponse(true);
    }

    @PostMapping("/token/refresh")
    public LoginResponse refreshToken(@RequestBody @Validated RefreshTokenRequest request,
            BindingResult result) {
        ValidationUtil.validate(result);
        UserDTO userDTO = userService.loginDevice(request.getDeviceId(), null, null);
        String jwtToken = voiceRoomJwtUtil.getJwt(userDTO.getUid());
        String imToken = tokenProvider.buildImToken(userDTO);
        return new LoginResponse(userDTO.getUid(), userDTO.getName(), userDTO.getPortrait(),
                userDTO.getChatUid(), jwtToken, imToken, userDTO.getRtcUid());
    }

}
