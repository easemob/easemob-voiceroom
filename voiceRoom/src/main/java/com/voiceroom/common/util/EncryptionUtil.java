package com.voiceroom.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

@Slf4j
@Component
public class EncryptionUtil {

    private static final String HEX_NUMS_STR = "0123456789ABCDEF";
    private static final Integer SALT_LENGTH = 16;

    private final Boolean isEncrypt;

    @Autowired
    public EncryptionUtil(@Value("${is.encrypt.password:false}") Boolean isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    /**
     * 密码验证
     * @param password
     * @param passwordInDb
     * @return
     */
    public boolean validPassword(String password, String passwordInDb) {
        if (!isEncrypt) {
            return password.equals(passwordInDb);
        }
        // 将16进制字符串格式口令转换成字节数组
        byte[] pwdInDb = hexStringToByte(passwordInDb);
        // 声明盐变量
        byte[] salt = new byte[SALT_LENGTH];
        // 将盐从数据库中保存的口令字节数组中提取出来
        System.arraycopy(pwdInDb, 0, salt, 0, SALT_LENGTH);
        // 创建消息摘要对象
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.warn("encrypt room password not found algorithm MD5");
            return password.equals(passwordInDb);
        }
        md.update(salt);
        md.update(password.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        byte[] digestInDb = new byte[pwdInDb.length - SALT_LENGTH];
        System.arraycopy(pwdInDb, SALT_LENGTH, digestInDb, 0, digestInDb.length);
        if (Arrays.equals(digest, digestInDb)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 加密
     * @param password
     * @return
     */
    public String getEncryptedPwd(String password) {
        if (!isEncrypt) {
            return password;
        }
        byte[] pwd;
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.warn("encrypt room password not found algorithm MD5");
            return password;
        }
        md.update(salt);
        md.update(password.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        pwd = new byte[digest.length + SALT_LENGTH];
        System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH);
        System.arraycopy(digest, 0, pwd, SALT_LENGTH, digest.length);
        return byteToHexString(pwd);
    }

    private byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] hexChars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (HEX_NUMS_STR.indexOf(hexChars[pos]) << 4 | HEX_NUMS_STR
                    .indexOf(hexChars[pos + 1]));
        }
        return result;
    }

    private String byteToHexString(byte[] b) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }
}
