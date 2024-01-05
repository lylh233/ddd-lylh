package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperOccupationService;
import com.lylh.repository.dao.OccupationMapper;
import com.lylh.repository.entity.OccupationDO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapperOccupationServiceImpl extends ServiceImpl<OccupationMapper, OccupationDO> implements MapperOccupationService {

    @Override
    public List<OccupationDO> getByUuids(List<String> uuids) {
        LambdaQueryWrapper<OccupationDO> queryWrapper = new LambdaQueryWrapper<OccupationDO>()
                .in(OccupationDO::getUuid, uuids);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public OccupationDO getByUuid(String uuid) {
        LambdaQueryWrapper<OccupationDO> queryWrapper = new LambdaQueryWrapper<OccupationDO>()
                .eq(OccupationDO::getUuid, uuid);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateOccupation(OccupationDO occupationDO, String name) {
        LambdaUpdateWrapper<OccupationDO> updateWrapper = new LambdaUpdateWrapper<OccupationDO>()
                .set(OccupationDO::getName, name)
                .eq(OccupationDO::getId, occupationDO.getId());
        baseMapper.update(occupationDO, updateWrapper);
    }

    @Override
    public void deleteByUuid(String uuid) {
        LambdaQueryWrapper<OccupationDO> queryWrapper = new LambdaQueryWrapper<OccupationDO>()
                .eq(OccupationDO::getUuid, uuid);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public List<OccupationDO> listOccupation(Integer limit) {
        LambdaQueryWrapper<OccupationDO> queryWrapper = new LambdaQueryWrapper<OccupationDO>();
        if (limit != null) {
            queryWrapper.last("limit " + limit);
        }
        return baseMapper.selectList(queryWrapper);
    }
}
