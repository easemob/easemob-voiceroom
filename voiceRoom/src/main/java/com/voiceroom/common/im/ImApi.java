package com.voiceroom.common.im;

import com.easemob.im.server.EMException;
import com.easemob.im.server.EMService;
import com.easemob.im.server.api.DefaultErrorMapper;
import com.easemob.im.server.api.ErrorMapper;
import com.easemob.im.server.api.metadata.chatroom.AutoDelete;
import com.easemob.im.server.api.metadata.chatroom.delete.ChatRoomMetadataDeleteResponse;
import com.easemob.im.server.api.metadata.chatroom.get.ChatRoomMetadataGetResponse;
import com.easemob.im.server.api.metadata.chatroom.set.ChatRoomMetadataSetResponse;
import com.easemob.im.server.api.token.Token;
import com.easemob.im.server.exception.EMForbiddenException;
import com.easemob.im.server.exception.EMNotFoundException;
import com.easemob.im.server.exception.EMUnknownException;
import com.easemob.im.server.model.EMKeyValue;
import com.easemob.im.server.model.EMPage;
import com.easemob.im.server.model.EMRoom;
import com.easemob.im.server.model.EMUser;
import com.easemob.im.shaded.io.netty.buffer.ByteBuf;
import com.easemob.im.shaded.io.netty.buffer.ByteBufUtil;
import com.easemob.im.shaded.io.netty.handler.timeout.TimeoutException;
import com.easemob.im.shaded.reactor.core.publisher.Mono;
import com.easemob.im.shaded.reactor.netty.http.client.HttpClient;
import com.easemob.im.shaded.reactor.netty.http.client.HttpClientResponse;
import com.voiceroom.mic.common.constants.CustomMetricsName;
import com.voiceroom.mic.model.UserThirdAccount;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * imAPI。
 */
@Slf4j
@Service
public class ImApi {

    @Resource
    private EMService emService;

    @Resource
    private PrometheusMeterRegistry registry;

    @Value("${http.request.timeout:PT10s}")
    private Duration timeout;

    @Value("${im.easemob.baseUri}")
    private String baseUrl;

    /**
     * 不指定密码创建用户
     *
     * @param uid
     * @param username
     * @return
     * @throws EMException
     */
    public UserThirdAccount createUser(@Nonnull String uid, @Nonnull String username)
            throws EMException {
        return createUser(uid, username, null);
    }

    /**
     * 创建用户
     *
     * @param uid
     * @param username
     * @param password
     * @return
     */
    public UserThirdAccount createUser(@Nonnull String uid, @Nonnull String username,
            String password) {
        if (StringUtils.isBlank(password)) {
            password = UUID.randomUUID().toString().replace("-", "");
        }
        Instant startTimeStamp = Instant.now();
        try {
            EMUser emUser = this.emService.user().create(username, password).block();
            addMetricsTimerRecord("createUser", Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("createUser");
            return UserThirdAccount.builder().uid(uid).chatId(emUser.getUsername())
                    .chatUuid(emUser.getUuid()).build();
        } catch (TimeoutException e) {
            log.error("createUser request timeout | uid={}, username={}",
                    uid, username, e);
            addErrorMetricsRecord("createUser", "timeout");

        } catch (EMException e) {
            log.error("createUser request easemob failed,userName:{},uid:{}", username, uid, e);
            addErrorMetricsRecord("createUser", "InternalServerError");
        } catch (Exception e) {
            log.error("createUser failed | uid={}, username={}",
                    uid, username, e);
            addErrorMetricsRecord("createUser", "unknown");
        }
        return null;

    }

    /**
     * 删除用户
     *
     * @param userName: 用户名
     * @throws EMException
     */
    public void deleteUser(@Nonnull String userName)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            this.emService.user().delete(userName).block();
            addMetricsTimerRecord("deleteUser", Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("deleteUser");
        } catch (TimeoutException e) {
            log.error("deleteUser request timeout | username={}",
                    userName, e);
            addErrorMetricsRecord("deleteUser", "timeout");
        } catch (EMException e) {
            log.error("deleteUser request easemob failed,userName:{}", userName, e);
            addErrorMetricsRecord("deleteUser", "InternalServerError");
        } catch (Exception e) {
            log.error("deleteUser failed | username={}",
                    userName, e);
            addErrorMetricsRecord("deleteUser", "unknown");
        }

    }

    /**
     * 创建聊天室
     *
     * @param chatRoomName:    聊天室的名称
     * @param owner:           房主
     * @param members:         成员列表
     * @param description:房间描述
     * @return String :聊天室id
     * @throws EMException
     */
    public String createChatRoom(@Nonnull String chatRoomName, @Nonnull String owner,
            @Nonnull List<String> members,
            @Nonnull String description)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            String chatroomId =
                    emService.room().createRoom(chatRoomName, description, owner, members, 200)
                            .block();
            addMetricsTimerRecord("createChatRoom",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("createChatRoom");
            return chatroomId;
        } catch (TimeoutException e) {
            log.error(
                    "createRoom request timeout,chatRoomName:{},owner:{},members:{},description:{}",
                    chatRoomName, owner, members, description, e);
            addErrorMetricsRecord("createChatRoom", "timeout");
        } catch (EMException e) {
            log.error(
                    "createRoom request easemob failed,chatRoomName:{},owner:{},members:{},description:{}",
                    chatRoomName, owner, members, description, e);
            addErrorMetricsRecord("createChatRoom", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "createRoom failed,chatRoomName:{},owner:{},members:{},description:{}",
                    chatRoomName, owner, members, description, e);
            addErrorMetricsRecord("createChatRoom", "unknown");
        }
        return null;

    }

    /**
     * 销毁聊天室
     *
     * @param chatRoomId: 聊天室id
     * @throws EMException
     */
    public void deleteChatRoom(@Nonnull String chatRoomId)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            emService.room().destroyRoom(chatRoomId).block();
            addMetricsTimerRecord("deleteChatRoom",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("deleteChatRoom");
        } catch (TimeoutException e) {
            log.error(
                    "deleteChatRoom request timeout,chatRoomId:{}", chatRoomId, e);
            addErrorMetricsRecord("deleteChatRoom", "timeout");
        } catch (EMException e) {
            log.error(
                    "deleteChatRoom request easemob failed,chatRoomId:{}", chatRoomId, e);
            addErrorMetricsRecord("deleteChatRoom", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "deleteChatRoom failed,chatRoomId:{}", chatRoomId, e);
            addErrorMetricsRecord("deleteChatRoom", "unknown");
        }

    }

    /**
     * 获取聊天室详情
     *
     * @param chatRoomId: 聊天室id
     * @return EMRoom :聊天室详情
     * @throws EMException
     */
    public EMRoom getChatRoomInfo(@Nonnull String chatRoomId) throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            EMRoom emRoom = emService.room().getRoom(chatRoomId)
                    .block();
            addMetricsTimerRecord("getChatRoomInfo",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("getChatRoomInfo");
            return emRoom;
        } catch (TimeoutException e) {
            log.error(
                    "getChatRoomInfo request timeout,chatRoomId:{}", chatRoomId, e);
            addErrorMetricsRecord("getChatRoomInfo", "timeout");
        } catch (EMException e) {
            log.error(
                    "getChatRoomInfo request easemob failed,chatRoomId:{}", chatRoomId, e);
            addErrorMetricsRecord("getChatRoomInfo", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "getChatRoomInfo failed,chatRoomId:{}", chatRoomId, e);
            addErrorMetricsRecord("getChatRoomInfo", "unknown");
        }
        return null;
    }

    /**
     * 分页获取聊天室列表
     *
     * @param limit  返回多少个聊天室id
     * @param cursor 开始位置
     * @return 聊天室id列表和cursor
     * @throws EMException
     */
    public EMPage<String> listChatRooms(int limit, String cursor)
            throws EMException {

        Instant startTimeStamp = Instant.now();
        try {
            EMPage<String> pageInfo = emService.room().listRooms(limit, cursor).block();
            addMetricsTimerRecord("listChatRooms", Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("listChatRooms");
            return pageInfo;
        } catch (TimeoutException e) {
            log.error(
                    "listChatRooms request timeout,listChatRooms error,limit:{},cursor:{},members:{},description:{}",
                    limit, cursor, e);
            addErrorMetricsRecord("listChatRooms", "timeout");
        } catch (EMException e) {
            log.error(
                    "listChatRooms request easemob failed,listChatRooms error,limit:{},cursor:{},members:{},description:{}",
                    limit, cursor, e);
            addErrorMetricsRecord("listChatRooms", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "listChatRooms failed,limit:{},cursor:{},members:{},description:{}",
                    limit, cursor, e);
            addErrorMetricsRecord("listChatRooms", "unknown");
        }
        return null;

    }

    /**
     * 分页获取聊天室成员列表
     *
     * @param chatRoomId 聊天室id
     * @param limit      返回多少个聊天室成员
     * @param cursor     开始位置
     * @param sort       聊天室成员排序方法 asc:根据加入顺序升序排序  desc:根据加入顺序降序排序
     * @return 聊天室用户的userName列表和cursor
     * @throws EMException
     */
    public EMPage<String> listChatRoomMembers(@Nonnull String chatRoomId, int limit, String cursor,
            String sort)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            EMPage<String> pageInfo =
                    emService.room().listRoomMembers(chatRoomId, limit, cursor, sort).block();
            addMetricsTimerRecord("listChatRoomMembers",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("listChatRoomMembers");
            return pageInfo;
        } catch (TimeoutException e) {
            log.error(
                    "listChatRoomMembers request timeout,listChatRoomMembers error,chatRoomId:{},limit:{},cursor:{}",
                    chatRoomId, limit, cursor, e);
            addErrorMetricsRecord("listChatRooms", "timeout");
        } catch (EMException e) {
            log.error("listChatRoomMembers request easemob failed,chatRoomId:{},limit:{},cursor:{}",
                    chatRoomId, limit, cursor, e);
            addErrorMetricsRecord("listChatRoomMembers", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "listChatRoomMembers failed,listChatRoomMembers error,chatRoomId:{},limit:{},cursor:{}",
                    chatRoomId, limit, cursor, e);
            addErrorMetricsRecord("listChatRoomMembers", "unknown");
        }
        return null;

    }

    /**
     * 从聊天室移除成员。
     *
     * @param chatRoomId 聊天室id
     * @param userName   聊天室成员
     * @throws EMException
     */
    public void removeChatRoomMember(@Nonnull String chatRoomId, @Nonnull String userName)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            emService.room().removeRoomMember(chatRoomId, userName).block();
            addMetricsTimerRecord("removeChatRoomMember",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("removeChatRoomMember");
        } catch (TimeoutException e) {
            log.error(
                    "removeChatRoomMember request timeout,removeChatRoomMember error,chatRoomId:{},userName:{}",
                    chatRoomId, userName, e);
            addErrorMetricsRecord("removeChatRoomMember", "timeout");
        } catch (EMException e) {
            log.error("removeChatRoomMember request easemob failed,chatRoomId:{},userName:{}",
                    chatRoomId, userName, e);
            addErrorMetricsRecord("removeChatRoomMember", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "removeChatRoomMember failed,removeChatRoomMember error,chatRoomId:{},userName:{}",
                    chatRoomId, userName, e);
            addErrorMetricsRecord("removeChatRoomMember", "unknown");
        }
    }

    /**
     * 设置群公告
     *
     * @param chatRoomId   聊天室id
     * @param announcement 通知
     * @throws EMException
     */
    public void setAnnouncement(@Nonnull String chatRoomId, @Nonnull String announcement)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            emService.room().updateRoomAnnouncement(chatRoomId, announcement).block();
            addMetricsTimerRecord("setAnnouncement",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("setAnnouncement");
        } catch (TimeoutException e) {
            log.error(
                    "setAnnouncement request timeout,chatRoomId:{},announcement:{}",
                    chatRoomId, announcement, e);
            addErrorMetricsRecord("setAnnouncement", "timeout");
        } catch (EMException e) {
            log.error("setAnnouncement request easemob failed,chatRoomId:{},announcement:{}",
                    chatRoomId, announcement, e);
            addErrorMetricsRecord("setAnnouncement", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "setAnnouncement failed,chatRoomId:{},announcement:{}",
                    chatRoomId, announcement, e);
            addErrorMetricsRecord("removeChatRoomMember", "unknown");
        }
    }

    /**
     * 获取群公告
     *
     * @param chatRoomId 聊天室id
     * @throws EMException
     */
    public String getAnnouncement(@Nonnull String chatRoomId)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            String announcement = emService.room().getRoomAnnouncement((chatRoomId)).block();
            addMetricsTimerRecord("getAnnouncement",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("getAnnouncement");
            return announcement;
        } catch (EMException e) {
            log.error("server error,getAnnouncement error,chatRoomId:{}",
                    chatRoomId, e);
            addErrorMetricsRecord("getAnnouncement", "InternalServerError");
        }
        return null;
    }

    /**
     * 发送自定义消息
     *
     * @param fromUserName     发送的成员
     * @param toChatRoomId     接收的聊天室id
     * @param customEvent      自定义消息类型
     * @param customExtensions 自定义消息内容
     * @param extension        自定义消息扩展
     * @throws EMException
     */
    public void sendChatRoomCustomMessage(@Nonnull String fromUserName,
            @Nonnull String toChatRoomId,
            @Nonnull String customEvent, @Nonnull Map<String, Object> customExtensions,
            Map<String, Object> extension)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            emService.message().send()
                    .fromUser(fromUserName)
                    .toRoom(toChatRoomId)
                    .custom(msg -> msg.customEvent(customEvent)
                            .customExtensions(EMKeyValue.of(customExtensions)))
                    .extension(msg -> msg.addAll(EMKeyValue.of(extension)))
                    .send()
                    .block();
            addMetricsTimerRecord("sendChatRoomCustomMessage",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("sendChatRoomCustomMessage");
        } catch (TimeoutException e) {
            log.error(
                    "sendChatRoomCustomMessage request timeout,fromUserName:{},toChatRoomId:{},customEvent:{},customExtensions:{}",
                    fromUserName, toChatRoomId, customEvent, customExtensions, e);
            addErrorMetricsRecord("sendChatRoomCustomMessage", "timeout");
        } catch (EMException e) {
            log.error(
                    "sendChatRoomCustomMessage request easemob failed,fromUserName:{},toChatRoomId:{},customEvent:{},customExtensions:{}",
                    fromUserName, toChatRoomId, customEvent, customExtensions, e);
            addErrorMetricsRecord("sendChatRoomCustomMessage", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "sendChatRoomCustomMessage failed,,fromUserName:{},toChatRoomId:{},customEvent:{},customExtensions:{}",
                    fromUserName, toChatRoomId, customEvent, customExtensions, e);
            addErrorMetricsRecord("sendChatRoomCustomMessage", "unknown");
        }

    }

    /**
     * 发送自定义消息 个人
     *
     * @param fromUserName     发送的成员
     * @param toUserName       接收的成员
     * @param customEvent      自定义消息类型
     * @param customExtensions 自定义消息内容
     * @param extension        自定义消息扩展
     * @throws EMException
     */
    public void sendUserCustomMessage(@Nonnull String fromUserName,
            @Nonnull String toUserName,
            @Nonnull String customEvent, @Nonnull Map<String, Object> customExtensions,
            Map<String, Object> extension)
            throws EMException {

        Instant startTimeStamp = Instant.now();
        try {
            emService.message().send()
                    .fromUser(fromUserName)
                    .toUser(toUserName)
                    .custom(msg -> msg.customEvent(customEvent)
                            .customExtensions(EMKeyValue.of(customExtensions)))
                    .extension(msg -> msg.addAll(EMKeyValue.of(extension)))
                    .send()
                    .block();
            addMetricsTimerRecord("sendUserCustomMessage",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("sendUserCustomMessage");
        } catch (TimeoutException e) {
            log.error(
                    "sendUserCustomMessage request timeout,fromUserName:{},toUserName:{},customEvent:{},customExtensions:{}",
                    fromUserName, toUserName, customEvent, customExtensions, e);
            addErrorMetricsRecord("sendUserCustomMessage", "timeout");
        } catch (EMException e) {
            log.error(
                    "sendUserCustomMessage request easemob failed,fromUserName:{},toUserName:{},customEvent:{},customExtensions:{}",
                    fromUserName, toUserName, customEvent, customExtensions, e);
            addErrorMetricsRecord("sendUserCustomMessage", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "sendUserCustomMessage failed,chatRoomId:{},fromUserName:{},toUserName:{},customEvent:{},customExtensions:{}",
                    fromUserName, toUserName, customEvent, customExtensions, e);
            addErrorMetricsRecord("sendUserCustomMessage", "unknown");
        }

    }

    /**
     * 设置聊天室属性
     *
     * @param operator   操作人
     * @param chatRoomId 接收的聊天室id
     * @param metadata   属性k-v
     * @throws EMException
     */
    public ChatRoomMetadataSetResponse setChatRoomMetadata(@Nonnull String operator,
            @Nonnull String chatRoomId,
            @Nonnull Map<String, String> metadata,
            AutoDelete autoDelete)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            ChatRoomMetadataSetResponse response = emService.metadata()
                    .setChatRoomMetadata(operator, chatRoomId, metadata, autoDelete)
                    .block();
            addMetricsTimerRecord("setChatRoomMetadata",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("setChatRoomMetadata");
            return response;
        } catch (TimeoutException e) {
            log.error(
                    "setChatRoomMetadata request timeout,fromUserName:{},operator:{},chatRoomId:{},metadata:{},autoDelete:{}",
                    operator, chatRoomId, metadata, autoDelete, e);
            addErrorMetricsRecord("setChatRoomMetadata", "timeout");
        } catch (EMException e) {
            log.error(
                    "setChatRoomMetadata request easemob failed,operator:{},chatRoomId:{},metadata:{},autoDelete:{}",
                    operator, chatRoomId, metadata, autoDelete, e);
            addErrorMetricsRecord("setChatRoomMetadata", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "setChatRoomMetadata failed,chatRoomId:{},operator:{},chatRoomId:{},metadata:{},autoDelete:{}",
                    operator, chatRoomId, metadata, autoDelete, e);
            addErrorMetricsRecord("setChatRoomMetadata", "unknown");
        }
        return null;

    }

    /**
     * 删除聊天室属性
     *
     * @param operator   操作人
     * @param chatRoomId 接收的聊天室id
     * @param keys       属性k列表
     * @throws EMException
     */
    public ChatRoomMetadataDeleteResponse deleteChatRoomMetadata(@Nonnull String operator,
            @Nonnull String chatRoomId,
            @Nonnull List<String> keys)
            throws EMException {

        Instant startTimeStamp = Instant.now();
        try {
            ChatRoomMetadataDeleteResponse response =
                    emService.metadata().deleteChatRoomMetadata(operator, chatRoomId, keys)
                            .block();
            addMetricsTimerRecord("deleteChatRoomMetadata",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("deleteChatRoomMetadata");
            return response;
        } catch (TimeoutException e) {
            log.error(
                    "deleteChatRoomMetadata request timeout,fromUserName:{},operator:{},chatRoomId:{},keys:{}",
                    operator, chatRoomId, keys, e);
            addErrorMetricsRecord("deleteChatRoomMetadata", "timeout");
        } catch (EMException e) {
            log.error(
                    "deleteChatRoomMetadata request easemob failed,operator:{},chatRoomId:{},keys:{}",
                    operator, chatRoomId, keys, e);
            addErrorMetricsRecord("deleteChatRoomMetadata", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "deleteChatRoomMetadata failed,chatRoomId:{},operator:{},chatRoomId:{},keys:{}",
                    operator, chatRoomId, keys, e);
            addErrorMetricsRecord("deleteChatRoomMetadata", "unknown");
        }
        return null;

    }

    /**
     * 获取聊天室属性
     *
     * @param chatRoomId 接收的聊天室id
     * @param keys       属性k列表
     * @throws EMException
     */
    public ChatRoomMetadataGetResponse listChatRoomMetadata(@Nonnull String chatRoomId,
            List<String> keys)
            throws EMException {
        Instant startTimeStamp = Instant.now();
        try {
            ChatRoomMetadataGetResponse response =
                    emService.metadata().listChatRoomMetadata(chatRoomId, keys)
                            .block();
            addMetricsTimerRecord("listChatRoomMetadata",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("listChatRoomMetadata");
            return response;
        } catch (TimeoutException e) {
            log.error(
                    "listChatRoomMetadata request timeout,chatRoomId:{},keys:{}", chatRoomId,
                    keys, e);
            addErrorMetricsRecord("listChatRoomMetadata", "timeout");
        } catch (EMException e) {
            log.error("listChatRoomMetadata request easemob failed,chatRoomId:{},keys:{}",
                    chatRoomId,
                    keys, e);
            addErrorMetricsRecord("listChatRoomMetadata", "InternalServerError");
        } catch (Exception e) {
            log.error(
                    "listChatRoomMetadata failed,chatRoomId:{},keys:{}", chatRoomId,
                    keys, e);
            addErrorMetricsRecord("listChatRoomMetadata", "unknown");
        }
        return null;

    }

    public void kickChatroomMember(String chatroomId, String username) {
        Instant startTimeStamp = Instant.now();
        try {
            emService.room().removeRoomMember(chatroomId, username)
                    .timeout(timeout)
                    .block();
            addMetricsTimerRecord("kickChatroomMember",
                    Duration.between(startTimeStamp, Instant.now()));
            addMetricsRecord("kickChatroomMember");
        } catch (TimeoutException e) {
            log.error("kickChatroomMember request timeout | chatroomId={}, username={}",
                    chatroomId, username, e);
            addErrorMetricsRecord("kickChatroomMember", "timeout");
        } catch (EMException e) {
            if (e instanceof EMForbiddenException) {
                log.warn("kickChatroomMember not in chatroom | chatroomId={}, username={}",
                        chatroomId, username);
                addErrorMetricsRecord("kickChatroomMember", "notInRoom");
            } else {
                log.error("kickChatroomMember request easemob failed | chatroomId={}, username={}",
                        chatroomId, username, e);
                addErrorMetricsRecord("kickChatroomMember", "InternalServerError");
            }
        } catch (Exception e) {
            log.error("kickChatroomMember failed | chatroomId={}, username={}",
                    chatroomId, username, e);
            addErrorMetricsRecord("kickChatroomMember", "unknown");
        }

    }

    public void sendSmsCode(String phone, String code) {
        // Self-implement mobile phone verification code function
        log.info("send sms code success , phone is {}, code is {}", phone, code);
    }

    private void addMetricsTimerRecord(String method, Duration duration) {
        List<Tag> tags = new ArrayList<>();
        Tag uriTag = Tag.of("method", method);
        tags.add(uriTag);
        registry.timer(CustomMetricsName.ImHttpRequestTimer, tags).record(duration);
    }

    private void addErrorMetricsRecord(String method, String errorMessage) {
        List<Tag> tags = getErrorTags(method, errorMessage);
        registry.counter(CustomMetricsName.ImHttpRequestCounter, tags).increment();
    }

    private void addMetricsRecord(String method) {
        List<Tag> tags = getSuccessTags(method);
        registry.counter(CustomMetricsName.ImHttpRequestCounter, tags).increment();
    }

    private List<Tag> getSuccessTags(String method) {
        List<Tag> tags = new ArrayList<>();
        Tag uriTag = Tag.of("method", method);
        Tag errorTag = Tag.of("reason", "success");
        Tag failedTag = Tag.of("result", "success");
        tags.add(errorTag);
        tags.add(failedTag);
        tags.add(uriTag);
        return tags;
    }

    private List<Tag> getErrorTags(String method, String reason) {
        List<Tag> tags = new ArrayList<>();
        Tag uriTag = Tag.of("method", method);
        Tag errorTag = Tag.of("reason", reason);
        Tag failedTag = Tag.of("result", "error");
        tags.add(errorTag);
        tags.add(failedTag);
        tags.add(uriTag);
        return tags;
    }

    public String createUserToken(String chatUid) {
        return emService.token().getUserTokenWithInherit(chatUid);
    }
}
