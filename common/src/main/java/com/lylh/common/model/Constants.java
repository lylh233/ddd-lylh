package com.lylh.common.model;

/**
 * 进程内固定约定。密钥同时参与存量密码和已签发令牌，轮换前必须先迁移数据。
 */
public class Constants {

    public static final String SECRET_PASSWORD = "1234567890123456";
    public static final String SECRET_TOKEN = "1234567890";

    public static final long TOKEN_EXP_TTL = 2 * 3600L; //单位秒
    public static final String TOKEN_HEADER = "ly-Token";

    public static final String TOKEN_UUID = "uuid";
    public static final String TOKEN_NAME = "name";
    public static final String TOKEN_USERNAME = "username";
    public static final String TOKEN_USER_TYPE = "user_type";

}
