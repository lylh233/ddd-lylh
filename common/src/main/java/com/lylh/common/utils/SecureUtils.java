package com.lylh.common.utils;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 密码密文工具。算法和密钥必须与存量数据一致，否则已有用户无法登录。
 */
@Slf4j
public class SecureUtils extends SecureUtil {

    /**
     * @param str 明文
     * @param key 密钥，长度需满足 AES
     * @return 十六进制密文
     */
    public static String encryptWithAES(String str, String key) {
        return aes(key.getBytes(StandardCharsets.UTF_8)).encryptHex(str);
    }

    /**
     * @param encryptStr 十六进制密文
     * @param key        与加密时相同的密钥
     * @return 明文
     */
    public static String decryptWithAES(String encryptStr, String key) {
        return aes(key.getBytes(StandardCharsets.UTF_8)).decryptStr(encryptStr);
    }

}
