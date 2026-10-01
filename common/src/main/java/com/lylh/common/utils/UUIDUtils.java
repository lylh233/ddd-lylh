package com.lylh.common.utils;

import cn.hutool.core.util.IdUtil;

import java.util.UUID;

/**
 * 按名称生成稳定的 UUID。同一名称每次结果相同，适合做可重复的业务标识。
 */
public class UUIDUtils extends IdUtil {

    /**
     * @param name 名称
     * @return 带连字符的 UUID
     */
    public static String generateByName(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public static String generateByNameWithoutHyphen(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString().replace("-", "");
    }

}
