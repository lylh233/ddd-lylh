package com.lylh.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.convert.UserConvert;
import com.lylh.biz.model.common.ServiceUtils;
import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.biz.service.UserService;
import com.lylh.common.interceptor.AuthInterceptor;
import com.lylh.common.model.BizException;
import com.lylh.common.model.Constants;
import com.lylh.common.model.ResponseCode;
import com.lylh.common.model.TokenPayload;
import com.lylh.common.utils.JwtTokenUtils;
import com.lylh.common.utils.SecureUtils;
import com.lylh.repository.biz.MapperUserService;
import com.lylh.repository.biz.model.filters.UserFilter;
import com.lylh.repository.entity.UserDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户登录与资料维护。密码沿用存量 AES 密文比对，更换算法会让已有账号无法登录。
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final MapperUserService mapperUserService;

    public UserServiceImpl(MapperUserService mapperUserService) {
        this.mapperUserService = mapperUserService;
    }

    @Override
    public String login(String username, String password) {
        UserDO userDO = mapperUserService.getByUserName(username);
        if (userDO == null) {
            log.warn("用户登录失败, username={}, code={}", username, ResponseCode.NO_USER_DATA.getCode());
            throw new BizException(ResponseCode.NO_USER_DATA);
        }

        String encrypted = SecureUtils.encryptWithAES(password, Constants.SECRET_PASSWORD);
        if (!encrypted.equals(userDO.getPassword())) {
            log.warn("用户登录失败, username={}, code={}", username, ResponseCode.PASSWORD_ERROR.getCode());
            throw new BizException(ResponseCode.PASSWORD_ERROR);
        }
        log.info("用户登录成功, username={}", username);
        return JwtTokenUtils.creatToken(UserConvert.toTokenPayload(userDO));
    }

    @Override
    public UserVO getLogonUser(HttpServletRequest request) {
        TokenPayload tokenPayload = AuthInterceptor.geTokenPayload();
        if (tokenPayload == null) {
            throw new BizException(ResponseCode.NO_TOKEN);
        }
        return UserConvert.toUserVO(tokenPayload);
    }

    @Override
    public String createUser(UserDTO userDTO) {
        checkByUserName(userDTO.getUsername());
        UserDO userDO = UserConvert.toUserDO(userDTO);
        mapperUserService.save(userDO);
        return userDO.getUuid();
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        UserDO userDO = mapperUserService.getByUuid(userDTO.getUuid());
        if (userDO == null) {
            throw new BizException(ResponseCode.NO_USER_DATA);
        }

        if (!userDTO.getUsername().equals(userDO.getUsername())) {
            checkByUserName(userDTO.getUsername());
        }
        mapperUserService.updateUser(userDO, userDTO.getName(), userDTO.getUsername(), userDTO.getUserType());
    }

    @Override
    public Page<UserVO> queryUser(String filter, Integer pageNum, Integer pageSize) {
        UserFilter userFilter = parseFilter(filter);
        Page<UserVO> page = ServiceUtils.toPage(pageNum, pageSize);
        List<UserDO> userDOList = mapperUserService.queryUserList(userFilter, ServiceUtils.toMapperPage(page));
        if (CollectionUtils.isEmpty(userDOList)) {
            return page;
        }
        long count = mapperUserService.queryUserCount(userFilter);
        return ServiceUtils.getPage(page, userDOList.stream()
                .map(UserConvert::toUserVO).collect(Collectors.toList()), count);
    }

    @Override
    public List<UserVO> getByName(String name) {
        List<UserDO> userDOList = mapperUserService.getByName(name);
        if (CollectionUtils.isEmpty(userDOList)) {
            return Collections.emptyList();
        }
        return userDOList.stream()
                .map(UserConvert::toUserVO).collect(Collectors.toList());
    }

    /**
     * 空白条件按无筛选处理。非法 JSON 转成业务异常，避免把解析器异常直接抛到接口。
     */
    private UserFilter parseFilter(String filter) {
        if (!StringUtils.hasText(filter)) {
            return new UserFilter();
        }
        try {
            UserFilter userFilter = JSON.parseObject(filter, UserFilter.class);
            return userFilter == null ? new UserFilter() : userFilter;
        } catch (RuntimeException e) {
            log.warn("用户查询条件不是合法 JSON");
            throw new BizException(ResponseCode.FAIL, e);
        }
    }

    private void checkByUserName(String username) {
        UserDO userDO = mapperUserService.getByUserName(username);
        if (userDO != null) {
            throw new BizException(ResponseCode.DUPLICATE_USER_NAME);
        }
    }
}
