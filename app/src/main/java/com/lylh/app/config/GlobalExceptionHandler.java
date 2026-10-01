package com.lylh.app.config;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.utils.HandlerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 把异常收成统一响应。业务失败只记原因，未预期故障才带堆栈，避免登录失败刷满错误日志。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未预期故障。对外只返回固定文案，细节留在日志里。
     *
     * @param request 当前请求
     * @param e       原始异常
     * @return 失败响应，不含异常细节
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(HttpServletRequest request, Exception e) {
        log.error("请求失败, url={}, method={}, params={}",
                request.getRequestURL(),
                request.getMethod(),
                HandlerUtils.getParamsFromHttpRequest(request),
                e);
        return new CommonResult<>(ResponseCode.FAIL.getCode(), "系统错误，请联系管理员", null);
    }

    /**
     * 可预期的业务失败。调用方按错误码处理，不需要错误级别告警。
     *
     * @param e 业务异常
     * @return 带业务码的失败响应
     */
    @ExceptionHandler(BizException.class)
    public CommonResult<String> handleBizException(BizException e) {
        if (e.getCause() != null) {
            log.warn("业务异常, code={}, message={}", e.getCode(), e.getMessage(), e);
        } else {
            log.warn("业务异常, code={}, message={}", e.getCode(), e.getMessage());
        }
        return new CommonResult<>(e.getCode(), e.getMessage(), null);
    }
}
