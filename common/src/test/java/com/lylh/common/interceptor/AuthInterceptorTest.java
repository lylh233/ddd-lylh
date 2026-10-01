package com.lylh.common.interceptor;

import com.lylh.common.controller.annotation.IgnorePermission;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.props.AuthConfigProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private AuthConfigProperties authConfigProperties;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @AfterEach
    void clearLoginContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        authInterceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);
    }

    @Test
    void should_skipVerification_when_switchOff() {
        when(authConfigProperties.isVerifyToken()).thenReturn(false);

        boolean passed = authInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertThat(passed).isTrue();
        assertThat(AuthInterceptor.geTokenPayload()).isNotNull();
    }

    @Test
    void should_skipToken_when_methodIgnoresPermission() throws Exception {
        when(authConfigProperties.isVerifyToken()).thenReturn(true);
        Method method = OpenApi.class.getMethod("ping");
        HandlerMethod handler = new HandlerMethod(new OpenApi(), method);

        boolean passed = authInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handler);

        assertThat(passed).isTrue();
        assertThat(AuthInterceptor.geTokenPayload()).isNull();
    }

    @Test
    void should_throwBizException_when_securedRequestHasNoToken() throws Exception {
        when(authConfigProperties.isVerifyToken()).thenReturn(true);
        Method method = SecuredApi.class.getMethod("ping");
        HandlerMethod handler = new HandlerMethod(new SecuredApi(), method);

        BizException exception = catchThrowableOfType(
                () -> authInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handler),
                BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.NO_TOKEN.getCode());
    }

    @IgnorePermission
    public static class OpenApi {
        public void ping() {
        }
    }

    public static class SecuredApi {
        public void ping() {
        }
    }
}
