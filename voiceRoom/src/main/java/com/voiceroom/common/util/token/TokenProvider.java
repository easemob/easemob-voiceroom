package com.voiceroom.common.util.token;

import com.voiceroom.common.im.ImApi;
import com.voiceroom.mic.pojos.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class TokenProvider {

    @Value("${agora.service.appId}")
    private String appId;

    @Value("${agora.service.appCert}")
    private String appCert;

    @Value("${agora.service.expireInSeconds:86400}")
    private int expireInSeconds;

    @Value("${use.easemob.token:false}")
    private boolean useEasemobToken;

    @Resource
    private ImApi imApi;

    public String buildImToken(UserDTO userDTO) {
        String token = null;
        if (useEasemobToken) {
            String chatUid = userDTO.getChatUid();
            token = imApi.createUserToken(chatUid);
        } else {
            String chatUuid = userDTO.getChatUuid();
            AccessToken2 accessToken = new AccessToken2(appId, appCert, expireInSeconds);
            AccessToken2.Service serviceChat = new AccessToken2.ServiceChat(chatUuid);
            serviceChat.addPrivilegeChat(AccessToken2.PrivilegeChat.PRIVILEGE_CHAT_USER,
                    expireInSeconds);
            accessToken.addService(serviceChat);
            try {
                token = accessToken.build();
            } catch (Exception e) {
                log.error("build im token failed | err=", e);
            }
        }

        return token;
    }

    public String buildRtcToken(UserDTO userDTO, String channelName) {
        Integer rtcUid = userDTO.getRtcUid();
        String account = AccessToken2.getUidStr(rtcUid);
        AccessToken2 accessToken = new AccessToken2(appId, appCert, expireInSeconds);
        AccessToken2.Service serviceRtc = new AccessToken2.ServiceRtc(channelName, account);

        serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_JOIN_CHANNEL, expireInSeconds);
        serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_AUDIO_STREAM, expireInSeconds);
        serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_VIDEO_STREAM, expireInSeconds);
        serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_DATA_STREAM, expireInSeconds);
        accessToken.addService(serviceRtc);
        try {
            return accessToken.build();
        } catch (Exception e) {
            log.error("build rtc token failed | err=", e);
            return null;
        }
    }
}
