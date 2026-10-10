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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final int USERNAME_QUERY_BATCH = 500;

    @Override
    public List<String> listExistingUsernames(Collection<String> usernames) {
        if (CollectionUtils.isEmpty(usernames)) {
            return Collections.emptyList();
        }
        List<String> existing = new ArrayList<>();
        List<String> batch = new ArrayList<>(USERNAME_QUERY_BATCH);
        for (String username : usernames) {
            batch.add(username);
            if (batch.size() == USERNAME_QUERY_BATCH) {
                existing.addAll(selectUsernames(new ArrayList<>(batch)));
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            existing.addAll(selectUsernames(batch));
        }
        return existing;
    }

    private List<String> selectUsernames(List<String> usernames) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<UserDO>()
                .select(UserDO::getUsername)
                .in(UserDO::getUsername, usernames);
        List<UserDO> users = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(UserDO::getUsername).collect(Collectors.toList());
    }

}
