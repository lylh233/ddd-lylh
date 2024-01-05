package com.lylh.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lylh.repository.entity.GroupDO;
import com.lylh.repository.entity.ext.GroupExtDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupMapper extends BaseMapper<GroupDO> {

    List<GroupExtDO> getGroupWithAssignmentByTeamUuid(@Param("teamUuid") String teamUuid);
}
