package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.entity.GroupDO;
import com.lylh.repository.entity.ext.GroupExtDO;

import java.util.List;

public interface MapperGroupService extends IService<GroupDO> {

    void deleteByTeamUuid(String teamUuid);

    List<GroupExtDO> getGroupWithAssignmentByTeamUuid(String teamUuid);
}
