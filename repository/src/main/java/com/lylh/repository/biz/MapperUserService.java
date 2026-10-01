package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.biz.model.filters.UserFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.entity.UserDO;

import java.util.List;

/**
 * 用户持久化操作。只负责读写，不判断登录或重名。
 */
public interface MapperUserService extends IService<UserDO> {

    UserDO getByUuid(String uuid);

    void updateUser(UserDO userDO, String name, String userName, Integer userType);

    List<UserDO> queryUserList(UserFilter userFilter, MapperPage mapperPage);

    long queryUserCount(UserFilter userFilter);

    UserDO getByUserName(String username);

    List<UserDO> getByName(String name);
}
