package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperTeamService;
import com.lylh.repository.dao.TeamMapper;
import com.lylh.repository.entity.TeamDO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperTeamServiceImpl extends ServiceImpl<TeamMapper, TeamDO> implements MapperTeamService {

    @Override
    public TeamDO getByUuid(String uuid) {
        LambdaQueryWrapper<TeamDO> queryWrapper = new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getUuid, uuid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateTeam(TeamDO teamDO, Integer serialNumber, String description) {
        LambdaUpdateWrapper<TeamDO> updateWrapper = new LambdaUpdateWrapper<TeamDO>()
                .set(TeamDO::getSerialNumber, serialNumber)
                .set(TeamDO::getDescription, description)
                .eq(TeamDO::getId, teamDO.getId());
        baseMapper.update(teamDO, updateWrapper);
    }

    @Override
    public void deleteByUuid(String uuid) {
        LambdaQueryWrapper<TeamDO> queryWrapper = new LambdaQueryWrapper<TeamDO>()
                .eq(TeamDO::getUuid, uuid);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public List<TeamDO> listTeam(Integer limit) {
        LambdaQueryWrapper<TeamDO> queryWrapper = new LambdaQueryWrapper<TeamDO>();
        if (limit != null) {
            queryWrapper.last("limit " + limit);
        }
        return baseMapper.selectList(queryWrapper);
    }
}
