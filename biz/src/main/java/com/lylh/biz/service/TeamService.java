package com.lylh.biz.service;

import com.lylh.biz.model.dto.TeamDTO;
import com.lylh.biz.model.vo.TeamVO;
import com.lylh.biz.model.vo.TeamWithGroupVO;

import java.util.List;

public interface TeamService {

    String create(TeamDTO teamDTO);

    void createBatch(List<TeamDTO> teamDTOList);

    void update(TeamDTO teamDTO);

    void updateBatch(List<TeamDTO> teamDTOList);

    void delete(String uuid);

    TeamVO getByUuid(String uuid);

    TeamWithGroupVO getTeamWithGroupByUuid(String uuid);

    List<TeamVO> listTeam(Integer limit);
}
