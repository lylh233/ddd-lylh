package com.lylh.biz.service.impl;

import com.lylh.biz.convert.GroupAssignmentConvert;
import com.lylh.biz.model.dto.GroupAssignmentDTO;
import com.lylh.biz.model.vo.GroupAssignmentVO;
import com.lylh.biz.service.GroupAssignmentService;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperGroupAssignmentService;
import com.lylh.repository.entity.GroupAssignmentDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupAssignmentServiceImpl implements GroupAssignmentService {

    @Autowired
    private MapperGroupAssignmentService mapperGroupAssignmentService;

    @Override
    public String create(GroupAssignmentDTO groupAssignmentDTO) {
        GroupAssignmentDO groupAssignmentDO = GroupAssignmentConvert.toGroupAssignmentDO(groupAssignmentDTO);
        mapperGroupAssignmentService.save(groupAssignmentDO);
        return groupAssignmentDO.getUuid();
    }

    @Override
    public void update(GroupAssignmentDTO groupAssignmentDTO) {
        GroupAssignmentDO groupAssignmentDO = mapperGroupAssignmentService.getByUuid(groupAssignmentDTO.getUuid());
        if (groupAssignmentDO == null) {
            throw new BizException(ResponseCode.NO_DATA);
        }
        mapperGroupAssignmentService.updateGroupAssignment(groupAssignmentDO, groupAssignmentDTO.getName(), groupAssignmentDTO.getDescription());
    }

    @Override
    public void delete(String uuid) {
        mapperGroupAssignmentService.deleteByUuid(uuid);
    }

    @Override
    public GroupAssignmentVO getByUuid(String uuid) {
        GroupAssignmentDO groupAssignmentDO = mapperGroupAssignmentService.getByUuid(uuid);
        if (groupAssignmentDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", uuid, ">");
        }
        return GroupAssignmentConvert.toGroupAssignmentVO(groupAssignmentDO);
    }

    @Override
    public List<GroupAssignmentVO> listGroupAssignment(Integer limit) {
        List<GroupAssignmentDO> groupAssignmentDOList = mapperGroupAssignmentService.listGroupAssignment(limit);
        if (CollectionUtils.isEmpty(groupAssignmentDOList)) {
            return Collections.emptyList();
        }
        return groupAssignmentDOList.stream()
                .map(GroupAssignmentConvert::toGroupAssignmentVO).collect(Collectors.toList());
    }
}
