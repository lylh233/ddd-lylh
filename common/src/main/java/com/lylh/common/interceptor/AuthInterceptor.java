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

/**
 * 在进入控制器前校验登录态，并把解析结果放进线程上下文。请求结束必须清理，避免线程池复用时串号。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<TokenPayload> TOKEN_PAYLOAD = new ThreadLocal<>();

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("StartTime", System.currentTimeMillis());
        // 关闭校验时仍放一个空登录态，下游读取时不会误判成未登录。
        if (!authConfigProperties.isVerifyToken()) {
            TOKEN_PAYLOAD.set(new TokenPayload());
            return true;
        }

        if (handler instanceof HandlerMethod && isIgnorePermission((HandlerMethod) handler)) {
            return true;
        }

        String token = JwtTokenUtils.getToken(request);
        verifyToken(token);
        TOKEN_PAYLOAD.set(JwtTokenUtils.parseToken(token));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        log.info("请求成功, url={}, method={}, params={}",
                request.getRequestURL(), request.getMethod(), HandlerUtils.getParamsFromHttpRequest(request));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TOKEN_PAYLOAD.remove();
        Object start = request.getAttribute("StartTime");
        if (start instanceof Long) {
            log.info("请求执行完毕, 总耗时:{}ms", System.currentTimeMillis() - (Long) start);
        }
    }

    /**
     * 读取当前线程的登录态。只能在拦截器放行后的同一请求线程里使用。
     *
     * @return 登录态，未写入时为 null
     */
    public static TokenPayload geTokenPayload() {
        return TOKEN_PAYLOAD.get();
    }

    /**
     * 先看是否缺失和是否过期，再验签名。过期令牌不再继续验签，避免把过期误报成签名无效。
     *
     * @param token 请求头中的令牌
     * @throws BizException 缺失、过期或签名无效
     */
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

    private boolean isIgnorePermission(HandlerMethod method) {
        return method.getMethodAnnotation(IgnorePermission.class) != null
                || method.getBeanType().getAnnotation(IgnorePermission.class) != null;
    }
}
