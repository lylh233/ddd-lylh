package com.lylh.common.utils;

import cn.hutool.core.util.IdUtil;

import java.util.UUID;

public class UUIDUtils extends IdUtil {

    public static String generateByName(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public static String generateByNameWithoutHyphen(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString().replace("-", "");
    }

}
