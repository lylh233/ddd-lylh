package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperGroupAssignmentService;
import com.lylh.repository.dao.GroupAssignmentMapper;
import com.lylh.repository.entity.GroupAssignmentDO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperGroupAssignmentServiceImpl extends ServiceImpl<GroupAssignmentMapper, GroupAssignmentDO> implements MapperGroupAssignmentService {

    @Override
    public GroupAssignmentDO getByUuid(String uuid) {
        LambdaQueryWrapper<GroupAssignmentDO> queryWrapper = new LambdaQueryWrapper<GroupAssignmentDO>()
                .eq(GroupAssignmentDO::getUuid, uuid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateGroupAssignment(GroupAssignmentDO groupAssignmentDO, String name, String description) {
        LambdaUpdateWrapper<GroupAssignmentDO> updateWrapper = new LambdaUpdateWrapper<GroupAssignmentDO>()
                .set(GroupAssignmentDO::getName, name)
                .set(GroupAssignmentDO::getDescription, description)
                .eq(GroupAssignmentDO::getId, groupAssignmentDO.getId());
        baseMapper.update(groupAssignmentDO, updateWrapper);
    }

    @Override
    public void deleteByUuid(String uuid) {
        LambdaQueryWrapper<GroupAssignmentDO> queryWrapper = new LambdaQueryWrapper<GroupAssignmentDO>()
                .eq(GroupAssignmentDO::getUuid, uuid);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public List<GroupAssignmentDO> listGroupAssignment(Integer limit) {
        LambdaQueryWrapper<GroupAssignmentDO> queryWrapper = new LambdaQueryWrapper<GroupAssignmentDO>();
        if (limit != null) {
            queryWrapper.last("limit " + limit);
        }
        return baseMapper.selectList(queryWrapper);
    }
}
