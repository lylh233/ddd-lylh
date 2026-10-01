package com.lylh.repository.biz.impl;

import com.lylh.repository.dao.UserMapper;
import com.lylh.repository.entity.UserDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapperUserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MapperUserServiceImpl mapperUserService;

    @Test
    void should_queryByUuid_when_userExists() {
        UserDO user = new UserDO();
        user.setUuid("u-1");
        when(userMapper.selectOne(any())).thenReturn(user);

        assertThat(mapperUserService.getByUuid("u-1").getUuid()).isEqualTo("u-1");
    }
}
