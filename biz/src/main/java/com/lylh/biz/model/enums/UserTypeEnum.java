package com.lylh.biz.model.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户类型。未知取值回落到无类型，避免接口因为脏数据直接失败。
 */
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

    /**
     * @param value 库存的类型值
     * @return 对应枚举，无法识别时返回 {@link #NULL}
     */
    public static UserTypeEnum parse(int value) {
        return Optional.ofNullable(map.get(value)).orElse(NULL);
    }

    public static List<UserTypeEnum> getEnumValues() {
        return Arrays.asList(values());
    }


}
