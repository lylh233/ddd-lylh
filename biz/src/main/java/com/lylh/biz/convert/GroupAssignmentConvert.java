package com.lylh.biz.convert;

import com.lylh.biz.model.dto.GroupAssignmentDTO;
import com.lylh.biz.model.vo.GroupAssignmentVO;
import com.lylh.common.utils.UUIDUtils;
import com.lylh.repository.entity.GroupAssignmentDO;

public class GroupAssignmentConvert {

    public static GroupAssignmentDO toGroupAssignmentDO(GroupAssignmentDTO groupAssignmentDTO) {
        GroupAssignmentDO groupAssignmentDO = new GroupAssignmentDO();
        groupAssignmentDO.setUuid(UUIDUtils.simpleUUID());
        groupAssignmentDO.setName(groupAssignmentDTO.getName());
        groupAssignmentDO.setDescription(groupAssignmentDTO.getDescription());
        return groupAssignmentDO;
    }

    public static GroupAssignmentVO toGroupAssignmentVO(GroupAssignmentDO groupAssignmentDO) {
        GroupAssignmentVO groupAssignmentVO = new GroupAssignmentVO();
        groupAssignmentVO.setUuid(groupAssignmentDO.getUuid());
        groupAssignmentVO.setName(groupAssignmentDO.getName());
        groupAssignmentVO.setDescription(groupAssignmentDO.getDescription());
        return groupAssignmentVO;
    }
}
