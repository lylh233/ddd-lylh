package com.lylh.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.enums.UserTypeEnum;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.biz.service.impl.UserServiceImpl;
import com.lylh.common.interceptor.AuthInterceptor;
import com.lylh.common.model.BizException;
import com.lylh.common.model.Constants;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.model.TokenPayload;
import com.lylh.common.utils.SecureUtils;
import com.lylh.repository.biz.MapperUserService;
import com.lylh.repository.biz.model.filters.UserFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.entity.UserDO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private MapperUserService mapperUserService;

    @AfterEach
    void clearLoginContext() throws Exception {
        bindLoginUser(null);
    }

    @Test
    void should_throwBizException_when_loginUserMissing() {
        when(mapperUserService.getByUserName("admin")).thenReturn(null);

        BizException exception = catchThrowableOfType(() -> userService.login("admin", "123456"), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.NO_USER_DATA.getCode());
    }

    @Test
    void should_throwBizException_when_loginPasswordMismatch() {
        when(mapperUserService.getByUserName("admin")).thenReturn(user("admin", "other-cipher"));

        BizException exception = catchThrowableOfType(() -> userService.login("admin", "123456"), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.PASSWORD_ERROR.getCode());
    }

    @Test
    void should_returnToken_when_loginPasswordMatches() {
        UserDO user = user("admin", SecureUtils.encryptWithAES("123456", Constants.SECRET_PASSWORD));
        when(mapperUserService.getByUserName("admin")).thenReturn(user);

        String token = userService.login("admin", "123456");

        assertThat(token).isNotBlank();
    }

    @Test
    void should_throwBizException_when_logonUserMissing() {
        BizException exception = catchThrowableOfType(() -> userService.getLogonUser(null), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.NO_TOKEN.getCode());
    }

    @Test
    void should_returnUser_when_logonUserPresent() throws Exception {
        TokenPayload payload = new TokenPayload();
        payload.setUuid("u-1");
        payload.setUsername("admin");
        payload.setName("浩");
        payload.setUserType(UserTypeEnum.STAFF.getType());
        bindLoginUser(payload);

        UserVO user = userService.getLogonUser(null);

        assertThat(user.getUuid()).isEqualTo("u-1");
        assertThat(user.getUserTypeDesc()).isEqualTo(UserTypeEnum.STAFF.getDesc());
    }

    @Test
    void should_throwBizException_when_createUserNameDuplicated() {
        when(mapperUserService.getByUserName("admin")).thenReturn(user("admin", "cipher"));

        BizException exception = catchThrowableOfType(() -> userService.createUser(userDto("admin")), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.DUPLICATE_USER_NAME.getCode());
        verify(mapperUserService, never()).save(any());
    }

    @Test
    void should_returnUuid_when_createUserSucceeds() {
        when(mapperUserService.getByUserName("admin")).thenReturn(null);

        String uuid = userService.createUser(userDto("admin"));

        ArgumentCaptor<UserDO> captor = ArgumentCaptor.forClass(UserDO.class);
        verify(mapperUserService).save(captor.capture());
        assertThat(uuid).isEqualTo(captor.getValue().getUuid()).isNotBlank();
        assertThat(captor.getValue().getPassword())
                .isEqualTo(SecureUtils.encryptWithAES("123456", Constants.SECRET_PASSWORD));
    }

    @Test
    void should_throwBizException_when_updateUserMissing() {
        when(mapperUserService.getByUuid("missing")).thenReturn(null);
        UserDTO dto = userDto("admin");
        dto.setUuid("missing");

        BizException exception = catchThrowableOfType(() -> userService.updateUser(dto), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.NO_USER_DATA.getCode());
    }

    @Test
    void should_throwBizException_when_updateUserRenamesToExistingName() {
        UserDO stored = user("admin", "cipher");
        stored.setUuid("u-1");
        when(mapperUserService.getByUuid("u-1")).thenReturn(stored);
        when(mapperUserService.getByUserName("other")).thenReturn(user("other", "cipher"));
        UserDTO dto = userDto("other");
        dto.setUuid("u-1");

        BizException exception = catchThrowableOfType(() -> userService.updateUser(dto), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.DUPLICATE_USER_NAME.getCode());
        verify(mapperUserService, never()).updateUser(any(), any(), any(), any());
    }

    @Test
    void should_updateUser_when_usernameUnchanged() {
        UserDO stored = user("admin", "cipher");
        stored.setId(8L);
        stored.setUuid("u-1");
        when(mapperUserService.getByUuid("u-1")).thenReturn(stored);
        UserDTO dto = userDto("admin");
        dto.setUuid("u-1");

        userService.updateUser(dto);

        verify(mapperUserService, never()).getByUserName("admin");
        verify(mapperUserService).updateUser(stored, "浩", "admin", UserTypeEnum.STAFF.getType());
    }

    @Test
    void should_returnEmptyPage_when_queryUserHasNoData() {
        when(mapperUserService.queryUserList(any(UserFilter.class), any(MapperPage.class)))
                .thenReturn(Collections.emptyList());

        Page<UserVO> page = userService.queryUser("{}", 1, 10);

        assertThat(page.getRecords()).isNullOrEmpty();
        verify(mapperUserService, never()).queryUserCount(any());
    }

    @Test
    void should_returnPage_when_queryUserHasData() {
        UserDO stored = user("admin", "cipher");
        stored.setUuid("u-1");
        stored.setName("浩");
        when(mapperUserService.queryUserList(any(UserFilter.class), any(MapperPage.class)))
                .thenReturn(Collections.singletonList(stored));
        when(mapperUserService.queryUserCount(any(UserFilter.class))).thenReturn(1L);

        Page<UserVO> page = userService.queryUser("{\"name\":\"浩\"}", 2, 10);

        assertThat(page.getTotal()).isEqualTo(1L);
        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getUsername()).isEqualTo("admin");
        assertThat(page.getCurrent()).isEqualTo(2L);
    }

    @Test
    void should_throwBizException_when_queryFilterInvalid() {
        BizException exception = catchThrowableOfType(() -> userService.queryUser("{", 1, 10), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.FAIL.getCode());
        assertThat(exception.getCause()).isNotNull();
    }

    @Test
    void should_returnEmptyList_when_getByNameMisses() {
        when(mapperUserService.getByName("浩")).thenReturn(Collections.emptyList());

        assertThat(userService.getByName("浩")).isEmpty();
    }

    @Test
    void should_returnUsers_when_getByNameHits() {
        UserDO stored = user("admin", "cipher");
        stored.setUuid("u-1");
        stored.setName("浩");
        when(mapperUserService.getByName("浩")).thenReturn(Collections.singletonList(stored));

        assertThat(userService.getByName("浩")).extracting(UserVO::getUuid).containsExactly("u-1");
    }

    private UserDO user(String username, String password) {
        UserDO user = new UserDO();
        user.setUsername(username);
        user.setPassword(password);
        user.setName("浩");
        user.setUserType(UserTypeEnum.STAFF.getType());
        return user;
    }

    private UserDTO userDto(String username) {
        UserDTO dto = new UserDTO();
        dto.setName("浩");
        dto.setUsername(username);
        dto.setPassword("123456");
        dto.setUserType(UserTypeEnum.STAFF.getType());
        return dto;
    }

    @SuppressWarnings("unchecked")
    private void bindLoginUser(TokenPayload payload) throws Exception {
        Field field = AuthInterceptor.class.getDeclaredField("TOKEN_PAYLOAD");
        field.setAccessible(true);
        ThreadLocal<TokenPayload> holder = (ThreadLocal<TokenPayload>) field.get(null);
        if (payload == null) {
            holder.remove();
        } else {
            holder.set(payload);
        }
    }
}
