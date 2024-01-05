package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperMemberService;
import com.lylh.repository.biz.model.filters.MemberFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.dao.MemberMapper;
import com.lylh.repository.entity.MemberDO;
import com.lylh.repository.entity.ext.MemberExtDO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperMemberServiceIml extends ServiceImpl<MemberMapper, MemberDO> implements MapperMemberService {

    @Override
    public MemberDO getByUuid(String uuid) {
        LambdaQueryWrapper<MemberDO> queryWrapper = new LambdaQueryWrapper<MemberDO>()
                .eq(MemberDO::getUuid, uuid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateMember(MemberDO memberDO, String name, String position, String occupationUuid) {
        LambdaUpdateWrapper<MemberDO> updateWrapper = new LambdaUpdateWrapper<MemberDO>()
                .set(MemberDO::getName, name)
                .set(MemberDO::getPosition, position)
                .set(MemberDO::getOccupationUuid, occupationUuid)
                .eq(MemberDO::getId, memberDO.getId());

        baseMapper.update(memberDO, updateWrapper);
    }

    @Override
    public void deleteByUuid(String uuid) {
        LambdaQueryWrapper<MemberDO> queryWrapper = new LambdaQueryWrapper<MemberDO>()
                .eq(MemberDO::getUuid, uuid);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public List<MemberExtDO> queryMemberList(MemberFilter memberFilter, MapperPage page) {
        return baseMapper.queryMemberList(memberFilter, page);
    }

    @Override
    public long queryMemberCount(MemberFilter memberFilter) {
        return baseMapper.queryMemberCount(memberFilter);
    }

    @Override
    public List<MemberExtDO> getMemberList(MemberFilter filter, Integer limit) {
        return baseMapper.getMemberList(filter, limit);
    }

    @Override
    public List<MemberExtDO> getMemberWithGroupListByGroupUuids(List<String> groupUuids) {
        return baseMapper.getMemberWithGroupListByGroupUuids(groupUuids);
    }
}
