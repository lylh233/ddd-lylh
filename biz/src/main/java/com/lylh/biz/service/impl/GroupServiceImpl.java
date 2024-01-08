package com.lylh.biz.service.impl;

import com.lylh.biz.convert.GroupConvert;
import com.lylh.biz.model.dto.GroupDTO;
import com.lylh.biz.model.vo.GroupVO;
import com.lylh.biz.model.vo.GroupWithMemberVO;
import com.lylh.biz.service.GroupService;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperGroupMemberAssociationService;
import com.lylh.repository.biz.MapperGroupService;
import com.lylh.repository.entity.GroupDO;
import com.lylh.repository.entity.ext.GroupExtDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private MapperGroupService mapperGroupService;

    @Autowired
    private MapperGroupMemberAssociationService mapperGroupMemberAssociationService;

    @Override
    public String create(GroupDTO groupDTO) {
        GroupDO groupDO = GroupConvert.toGroupDO(groupDTO, null);
        mapperGroupService.save(groupDO);
        return groupDO.getUuid();
    }

    @Override
    public void createBatch(List<GroupDTO> groupDTOList) {
        groupDTOList.forEach(this::create);
    }

    @Override
    public void update(GroupDTO groupDTO) {
        GroupDO groupDO = mapperGroupService.getByUuid(groupDTO.getUuid());
        if (groupDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", groupDTO.getUuid(), ">");
        }
        mapperGroupService.updateGroup(groupDO, groupDTO.getSerialNumber(), groupDTO.getGroupAssignmentUuid());
    }

    @Override
    public void updateBatch(List<GroupDTO> groupDTOList) {
        groupDTOList.forEach(this::update);
    }

    @Override
    public void delete(String uuid) {
        mapperGroupService.deleteByUuid(uuid);
        mapperGroupMemberAssociationService.deleteByGroupUuid(uuid);
    }

}
