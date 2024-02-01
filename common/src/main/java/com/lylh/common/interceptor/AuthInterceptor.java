package com.lylh.common.interceptor;

import com.lylh.common.controller.annotation.IgnorePermission;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.model.TokenPayload;
import com.lylh.common.props.AuthConfigProperties;
import com.lylh.common.utils.HandlerUtils;
import com.lylh.common.utils.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<TokenPayload> tokenPayload = new ThreadLocal<>();

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("url: {}", request.getRequestURL());
        request.setAttribute("StartTime", System.currentTimeMillis());
        //不需要验证，虚拟一个tokenPayload
        if (!authConfigProperties.isVerifyToken()) {
            tokenPayload.set(new TokenPayload());
            return true;
        }

        //加了@IgnorePermission注解的请求，以及加了@IgnorePermission注解的类下面的所有请求，都不处理token
        try {
            HandlerMethod method = (HandlerMethod) handler;
            if (Objects.nonNull(method.getMethodAnnotation(IgnorePermission.class)) || Objects.nonNull(method.getBeanType().getAnnotation(IgnorePermission.class))) {
                return true;
            }
        } catch (Exception ignore) {}

        String token = JwtTokenUtils.getToken(request);
        verifyToken(token);
        tokenPayload.set(JwtTokenUtils.parseToken(token));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("请求成功！地址：" + request.getRequestURL()
                + "，方法：" + request.getMethod()
                + "，参数：" + HandlerUtils.getParamsFromHttpRequest(request));
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tokenPayload.remove();
        Long start = (Long) request.getAttribute("StartTime");
        log.info("请求执行完毕 总耗时:{}", (System.currentTimeMillis() - start));
    }

    public static TokenPayload geTokenPayload() {
        return tokenPayload.get();
    }

    public static void verifyToken(String token) {
        if (token == null) {
            throw new BizException(ResponseCode.NO_TOKEN);
        }
        if (!JwtTokenUtils.verifyTokenExp(token)) {
            throw new BizException(ResponseCode.TOKEN_EXPIRED);
        }
        if (!JwtTokenUtils.verifyTokenValid(token)) {
            throw new BizException(ResponseCode.TOKEN_INVALID);
        }
    }
}
