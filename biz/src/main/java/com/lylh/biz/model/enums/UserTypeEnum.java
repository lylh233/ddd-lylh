package com.lylh.biz.model.enums;

import java.util.*;

public enum UserTypeEnum {

    NULL(0, "无类型"), //未知类型都会返回NULL,无类型‘
    PUBLIC_USER(1, "公众用户"),
    STAFF(2, "工作人员"),
    ;

    private final int type;
    private final String desc;

    UserTypeEnum(final int type, final String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    private static final Map<Integer, UserTypeEnum> map = new HashMap<>();

    static {
        for (UserTypeEnum userTypeEnum : values()) {
            map.put(userTypeEnum.type, userTypeEnum);
        }
    }

    public static UserTypeEnum parse(int value) {
        return Optional.ofNullable(map.get(value)).orElse(NULL);
    }

    public static List<UserTypeEnum> getEnumValues() {
        return Arrays.asList(values());
    }


}
