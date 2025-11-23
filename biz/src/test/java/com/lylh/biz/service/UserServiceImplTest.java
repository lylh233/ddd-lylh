package com.lylh.biz.service;

import com.lylh.biz.Fixture;
import com.lylh.biz.service.impl.UserServiceImpl;
import com.lylh.common.model.BizException;
import com.lylh.common.model.Constants;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.utils.SecureUtils;
import com.lylh.repository.biz.MapperUserService;
import com.lylh.repository.entity.UserDO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImplTest extends Fixture {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImplTest.class);

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private MapperUserService mapperUserService;

    @Test
    public void testLogin() {
        try {
            userService.login("admin", "123456");
        } catch (BizException e) {
            logger.info("用户名不存在，错误码：{}", ResponseCode.NO_USER_DATA.getCode());
            Assert.assertEquals(ResponseCode.NO_USER_DATA.getCode(), e.getCode());
        }

        UserDO userDO = new UserDO();
        userDO.setUsername("admin");
        Mockito.when(mapperUserService.getByUserName(Mockito.anyString())).thenReturn(userDO);
        try {
            userService.login("admin", "123456");
        } catch (BizException e) {
            logger.info("用户密码错误，错误码：{}", ResponseCode.PASSWORD_ERROR.getCode());
            Assert.assertEquals(ResponseCode.PASSWORD_ERROR.getCode(), e.getCode());
        }

        userDO.setPassword(SecureUtils.encryptWithAES("123456", Constants.SECRET_PASSWORD));
        String token = userService.login("admin", "123456");
        logger.info("登陆成功，用户token：{}", token);
        assert token != null;
    }
}
