package com.voiceroom.mic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.voiceroom.mic.model.User;
import com.voiceroom.mic.pojos.UserDTO;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param deviceId
     * @param name
     * @param portrait
     * @return
     */
    UserDTO loginDevice(String deviceId, String name, String portrait);

    UserDTO loginDeviceWithPhone(String deviceId, String name, String portrait, String phone);

    /**
     * 根据uid批量获取用户信息
     * @param ownerUidList
     * @return
     */
    Map<String, UserDTO> findByUidList(List<String> ownerUidList);

    /**
     * 根据uid获取指定用户信息
     * @param uid
     * @return
     */
    UserDTO getByUid(String uid);
}
