package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperGroupMemberAssociationService;
import com.lylh.repository.dao.GroupMemberAssociationMapper;
import com.lylh.repository.entity.GroupMemberAssociationDO;
import org.springframework.stereotype.Service;

@Service
public class MapperGroupMemberServiceImpl extends ServiceImpl<GroupMemberAssociationMapper, GroupMemberAssociationDO> implements MapperGroupMemberAssociationService {

    @Override
    public void deleteByMemberUuid(String memberUuid) {
        LambdaQueryWrapper<GroupMemberAssociationDO> queryWrapper = new LambdaQueryWrapper<GroupMemberAssociationDO>()
                .eq(GroupMemberAssociationDO::getMemberUuid, memberUuid);
        baseMapper.delete(queryWrapper);
    }
}
