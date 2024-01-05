package com.lylh.biz.service.impl;

import com.lylh.biz.convert.OccupationConvert;
import com.lylh.biz.model.dto.OccupationDTO;
import com.lylh.biz.model.vo.OccupationVO;
import com.lylh.biz.service.OccupationService;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperOccupationService;
import com.lylh.repository.entity.OccupationDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OccupationServiceImpl implements OccupationService {

    @Autowired
    private MapperOccupationService mapperOccupationService;

    @Override
    public String create(OccupationDTO occupationDTO) {
        OccupationDO occupationDO = OccupationConvert.toOccupationDO(occupationDTO);
        mapperOccupationService.save(occupationDO);
        return occupationDO.getUuid();
    }

    @Override
    public void update(OccupationDTO occupationDTO) {
        OccupationDO occupationDO = mapperOccupationService.getByUuid(occupationDTO.getUuid());
        if (occupationDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", occupationDTO.getUuid(), ">");
        }
        mapperOccupationService.updateOccupation(occupationDO, occupationDTO.getName());
    }

    @Override
    public void delete(String uuid) {
        mapperOccupationService.deleteByUuid(uuid);
    }

    @Override
    public OccupationVO getByUuid(String uuid) {
        OccupationDO occupationDO = mapperOccupationService.getByUuid(uuid);
        if (occupationDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", uuid, ">");
        }
        return OccupationConvert.toOccupationVO(occupationDO);
    }

    @Override
    public List<OccupationVO> listOccupation(Integer limit) {
        List<OccupationDO> occupationDOList = mapperOccupationService.listOccupation(limit);
        if (CollectionUtils.isEmpty(occupationDOList)) {
            return Collections.emptyList();
        }
        return occupationDOList.stream()
                .map(OccupationConvert::toOccupationVO).collect(Collectors.toList());
    }
}
