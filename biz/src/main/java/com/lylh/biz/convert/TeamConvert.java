package com.lylh.biz.convert;

import com.lylh.biz.model.dto.TeamDTO;
import com.lylh.biz.model.vo.TeamVO;
import com.lylh.common.utils.UUIDUtils;
import com.lylh.repository.entity.TeamDO;

public class TeamConvert {

    public static TeamDO toTeamDO(TeamDTO teamDTO) {
        TeamDO teamDO = new TeamDO();
        teamDO.setUuid(UUIDUtils.simpleUUID());
        teamDO.setSerialNumber(teamDTO.getSerialNumber());
        teamDO.setDescription(teamDTO.getDescription());
        return teamDO;
    }

    public static TeamVO toTeamVO(TeamDO teamDO) {
        TeamVO teamVO = new TeamVO();
        teamVO.setUuid(teamDO.getUuid());
        teamVO.setSerialNumber(teamDO.getSerialNumber());
        teamVO.setDescription(teamDO.getDescription());
        return teamVO;
    }
}
