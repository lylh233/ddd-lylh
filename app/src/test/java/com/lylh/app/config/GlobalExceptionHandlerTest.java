package com.lylh.app.config;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_hideDetail_when_unexpectedException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/login");
        request.addParameter("password", "123456");

        CommonResult<String> result = handler.handleException(request, new IllegalStateException("boom"));

        assertThat(result.getCode()).isEqualTo(ResponseCode.FAIL.getCode());
        assertThat(result.getMsg()).isEqualTo("系统错误，请联系管理员");
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).doesNotContain("boom");
    }

    @Test
    void should_keepBusinessCode_when_bizException() {
        CommonResult<String> result = handler.handleBizException(new BizException(ResponseCode.NO_USER_DATA));

        assertThat(result.getCode()).isEqualTo(ResponseCode.NO_USER_DATA.getCode());
        assertThat(result.getMsg()).isEqualTo(ResponseCode.NO_USER_DATA.getMessage());
    }
}
