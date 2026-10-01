package com.lylh.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class HandlerUtilsTest {

    @Test
    void should_maskPassword_when_collectRequestParams() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("username", "admin");
        request.addParameter("password", "123456");

        String params = HandlerUtils.getParamsFromHttpRequest(request);

        assertThat(params).contains("username:admin");
        assertThat(params).contains("password:***");
        assertThat(params).doesNotContain("123456");
    }
}
