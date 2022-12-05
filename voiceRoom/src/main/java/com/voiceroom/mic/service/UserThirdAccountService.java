package com.voiceroom.mic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.voiceroom.mic.model.UserThirdAccount;

public interface UserThirdAccountService extends IService<UserThirdAccount> {

    /**
     * 根据uid获取三方账户信息
     * @param uid
     * @return
     */
    UserThirdAccount getByUid(String uid);
}
