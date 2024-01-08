package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperGroupService;
import com.lylh.repository.dao.GroupMapper;
import com.lylh.repository.entity.GroupDO;
import com.lylh.repository.entity.ext.GroupExtDO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperGroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements MapperGroupService {

    @Override
    public void deleteByTeamUuid(String teamUuid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getTeamUuid, teamUuid);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public List<GroupExtDO> getGroupWithAssignmentByTeamUuid(String teamUuid) {
        return baseMapper.getGroupWithAssignmentByTeamUuid(teamUuid);
    }

    @Override
    public List<GroupExtDO> getGroupWithAssignmentByTeamUuids(List<String> teamUuids) {
        return baseMapper.getGroupWithAssignmentByTeamUuids(teamUuids);
    }

    @Override
    public GroupDO getByUuid(String uuid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUuid, uuid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateGroup(GroupDO groupDO, Integer serialNumber, String groupAssignmentUuid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = new LambdaUpdateWrapper<GroupDO>()
                .set(GroupDO::getSerialNumber, serialNumber)
                .set(GroupDO::getGroupAssignmentUuid, groupAssignmentUuid)
                .eq(GroupDO::getId, groupDO.getId());
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void deleteByUuid(String uuid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUuid, uuid);
        baseMapper.delete(queryWrapper);
    }
}
