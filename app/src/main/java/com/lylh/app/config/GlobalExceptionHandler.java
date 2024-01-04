package com.lylh.app.config;

import com.lylh.common.utils.HandlerUtils;
import com.lylh.common.model.BizException;
import com.lylh.biz.model.common.CommonResult;
import com.lylh.common.model.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(HttpServletRequest request, Exception e) {
        log(request, e);
        return new CommonResult<>(ResponseCode.FAIL.getCode(), "系统错误，请联系管理员", null);
    }

    @ExceptionHandler(BizException.class)
    public CommonResult<String> handleBizException(BizException e) {
        log.error("业务异常：{}", e.getMessage());
        return new CommonResult<>(e.getCode(), e.getMessage(), null);
    }

    public void log(HttpServletRequest request, Exception exception) {
        StringBuilder exStr = new StringBuilder();
        StackTraceElement[] trace = exception.getStackTrace();
        for (StackTraceElement s : trace) {
            exStr.append("\tat ").append(s).append("\r\n");
        }

        log.error("请求失败！地址：" + request.getRequestURL()
                + "，方法：" + request.getMethod()
                + "，参数：" + HandlerUtils.getParamsFromHttpRequest(request)
                + HandlerUtils.lineSeparatorStr()
                + exception
                + HandlerUtils.lineSeparatorStr()
                + exStr);
    }
}
