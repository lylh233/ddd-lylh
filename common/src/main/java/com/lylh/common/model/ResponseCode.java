package com.lylh.common.model;

public enum ResponseCode {

    SUCCESS(200, "请求成功!"),
    NO_TOKEN(401, "没有登录信息，请先登录!"),
    TOKEN_INVALID(401, "登录信息无效，请先登录!"),
    TOKEN_EXPIRED(401, "登录信息过期，请先登录!"),
    NO_PERMISSION(403, "无权限!"),
    FAIL(500, "请求失败!"),

    NO_DATA(40001, "数据不存在"),
    NO_USER_DATA(40001, "用户名不存在。"),
    DUPLICATE_USER_NAME(40002, "用户名称重复。"),
    PASSWORD_ERROR(40003, "用户密码错误。"),
    ;

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
