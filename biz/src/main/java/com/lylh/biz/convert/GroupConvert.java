package com.lylh.biz.convert;

import com.lylh.biz.model.dto.GroupDTO;
import com.lylh.biz.model.vo.GroupWithMemberVO;
import com.lylh.common.utils.UUIDUtils;
import com.lylh.repository.entity.GroupDO;
import com.lylh.repository.entity.ext.GroupExtDO;

public class GroupConvert {

    public static GroupDO toGroupDO(GroupDTO groupDTO, String teamUuid) {
        GroupDO groupDO = new GroupDO();
        groupDO.setUuid(UUIDUtils.simpleUUID());
        groupDO.setGroupAssignmentUuid(groupDTO.getGroupAssignmentUuid());
        groupDO.setSerialNumber(groupDTO.getSerialNumber());
        groupDO.setTeamUuid(teamUuid);

        return groupDO;
    }

    public static GroupWithMemberVO toGroupWithMemberVO(GroupExtDO groupExtDO) {
        GroupWithMemberVO groupWithMemberVO = new GroupWithMemberVO();
        groupWithMemberVO.setUuid(groupExtDO.getUuid());
        groupWithMemberVO.setTeamUuid(groupExtDO.getTeamUuid());
        groupWithMemberVO.setSerialNumber(groupExtDO.getSerialNumber());
        groupWithMemberVO.setGroupAssignmentName(groupExtDO.getGroupAssignmentName());

        return groupWithMemberVO;
    }
}
