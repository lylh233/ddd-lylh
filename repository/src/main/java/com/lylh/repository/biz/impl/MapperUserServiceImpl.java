package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperUserService;
import com.lylh.repository.biz.model.filters.UserFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.dao.UserMapper;
import com.lylh.repository.entity.UserDO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperUserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements MapperUserService {

    @Override
    public UserDO getByUuid(String uuid) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUuid, uuid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateUser(UserDO userDO, String name, String userName, Integer userType) {
        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<UserDO>()
                .set(UserDO::getName, name)
                .set(UserDO::getUsername, userName)
                .set(UserDO::getUserType, userType)
                .eq(UserDO::getId, userDO.getId());
        baseMapper.update(userDO, updateWrapper);
    }

    @Override
    public List<UserDO> queryUserList(UserFilter userFilter, MapperPage mapperPage) {
        return baseMapper.queryUserList(userFilter, mapperPage);
    }

    @Override
    public long queryUserCount(UserFilter userFilter) {
        return baseMapper.queryUserCount(userFilter);
    }

    @Override
    public UserDO getByUserName(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, username);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<UserDO> getByName(String name) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getName, name)
                .or()
                .eq(UserDO::getUsername, name);
        return baseMapper.selectList(queryWrapper);
    }

}
