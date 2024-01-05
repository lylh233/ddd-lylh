package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.entity.TeamDO;

import java.util.List;

public interface MapperTeamService extends IService<TeamDO> {

    TeamDO getByUuid(String uuid);

    void updateTeam(TeamDO teamDO, Integer serialNumber, String description);

    void deleteByUuid(String uuid);

    List<TeamDO> listTeam(Integer limit);
}
