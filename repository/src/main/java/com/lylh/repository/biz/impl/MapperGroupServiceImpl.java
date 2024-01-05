package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
