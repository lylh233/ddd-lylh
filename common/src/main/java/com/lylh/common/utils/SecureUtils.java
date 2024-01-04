package com.lylh.common.utils;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SecureUtils extends SecureUtil {

    public static String encryptWithAES(String str, String key) {
        return aes(key.getBytes(StandardCharsets.UTF_8)).encryptHex(str);
    }

    public static String decryptWithAES(String encryptStr, String key) {
        return aes(key.getBytes(StandardCharsets.UTF_8)).decryptStr(encryptStr);
    }

}
