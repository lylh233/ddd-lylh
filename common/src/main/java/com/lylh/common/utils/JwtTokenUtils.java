package com.lylh.common.utils;


import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import com.lylh.common.model.Constants;
import com.lylh.common.model.TokenPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 登录令牌的签发与校验。过期和签名分开判断，调用方才能返回不同错误码。
 */
@Slf4j
public class JwtTokenUtils {

    /**
     * 只把展示所需字段放进令牌。密码不进入载荷。
     *
     * @param tokenPayload 登录用户
     * @return 签名后的令牌
     */
    public static String creatToken(TokenPayload tokenPayload) {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TOKEN_UUID, tokenPayload.getUuid());
        map.put(Constants.TOKEN_NAME, tokenPayload.getName());
        map.put(Constants.TOKEN_USERNAME, tokenPayload.getUsername());
        map.put(Constants.TOKEN_USER_TYPE, tokenPayload.getUserType());

        //这里只设置过期时间
        map.put(JWTPayload.EXPIRES_AT, System.currentTimeMillis() / 1000 + Constants.TOKEN_EXP_TTL);
        //不传headers，默认加密算法hs256
        return JWTUtil.createToken(map, Constants.SECRET_TOKEN.getBytes(StandardCharsets.UTF_8));
    }

    public static String getToken(HttpServletRequest request) {
        return request.getHeader(Constants.TOKEN_HEADER);
    }


    public static TokenPayload parseToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        TokenPayload tokenPayload = new TokenPayload();
        final JWT jwt = JWTUtil.parseToken(token);

        tokenPayload.setUuid(Optional.ofNullable(jwt.getPayload(Constants.TOKEN_UUID)).map(Object::toString).orElse(null));
        tokenPayload.setName(Optional.ofNullable(jwt.getPayload(Constants.TOKEN_NAME)).map(Object::toString).orElse(null));
        tokenPayload.setUsername(Optional.ofNullable(jwt.getPayload(Constants.TOKEN_USERNAME)).map(Object::toString).orElse(null));
        tokenPayload.setUserType(Optional.ofNullable(jwt.getPayload(Constants.TOKEN_USER_TYPE)).map(item -> Integer.valueOf(item.toString())).orElse(null));
        return tokenPayload;
    }

    public static boolean verifyToken(String token) {
        return verifyTokenExp(token) && verifyTokenValid(token);
    }

    public static boolean verifyTokenExp(String token) {
        try {
            JWTValidator.of(token).validateDate();
            return true;
        } catch (Exception e) {
            // 缺失、过期或格式错误都按未通过处理，不把令牌原文打进日志。
            log.debug("token 未通过有效期校验, reason={}", e.toString());
            return false;
        }
    }

    public static boolean verifyTokenValid(String token) {
        return JWTUtil.verify(token, Constants.SECRET_TOKEN.getBytes(StandardCharsets.UTF_8));
    }
}
