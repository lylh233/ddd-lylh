package com.lylh.common.utils;

import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.lylh.common.interceptor.AuthInterceptor;
import com.lylh.common.model.BizException;
import com.lylh.common.model.Constants;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.model.TokenPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class JwtTokenUtilsTest {

    @Test
    void should_keepPayload_when_tokenIsValid() {
        TokenPayload payload = payload();

        String token = JwtTokenUtils.creatToken(payload);
        TokenPayload parsed = JwtTokenUtils.parseToken(token);

        assertThat(JwtTokenUtils.verifyToken(token)).isTrue();
        assertThat(parsed.getUuid()).isEqualTo("u-1");
        assertThat(parsed.getUsername()).isEqualTo("admin");
        assertThat(parsed.getUserType()).isEqualTo(2);
    }

    @Test
    void should_returnNull_when_tokenBlank() {
        assertThat(JwtTokenUtils.parseToken(" ")).isNull();
        assertThat(JwtTokenUtils.verifyTokenExp(" ")).isFalse();
    }

    @Test
    void should_rejectExpiredToken_when_expPassed() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWTPayload.EXPIRES_AT, System.currentTimeMillis() / 1000 - 10);
        String token = JWTUtil.createToken(claims, Constants.SECRET_TOKEN.getBytes(StandardCharsets.UTF_8));

        BizException exception = catchThrowableOfType(() -> AuthInterceptor.verifyToken(token), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.TOKEN_EXPIRED.getCode());
    }

    @Test
    void should_rejectSignature_when_secretMismatch() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWTPayload.EXPIRES_AT, System.currentTimeMillis() / 1000 + 60);
        String token = JWTUtil.createToken(claims, "other-secret".getBytes(StandardCharsets.UTF_8));

        BizException exception = catchThrowableOfType(() -> AuthInterceptor.verifyToken(token), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.TOKEN_INVALID.getCode());
    }

    @Test
    void should_throwBizException_when_tokenMissing() {
        BizException exception = catchThrowableOfType(() -> AuthInterceptor.verifyToken(null), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.NO_TOKEN.getCode());
    }

    private TokenPayload payload() {
        TokenPayload payload = new TokenPayload();
        payload.setUuid("u-1");
        payload.setName("浩");
        payload.setUsername("admin");
        payload.setUserType(2);
        return payload;
    }
}
