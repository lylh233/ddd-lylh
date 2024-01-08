package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.entity.GroupAssignmentDO;

import java.util.List;

public interface MapperGroupAssignmentService extends IService<GroupAssignmentDO> {

    GroupAssignmentDO getByUuid(String uuid);

    void updateGroupAssignment(GroupAssignmentDO groupAssignmentDO, String name, String description);

    void deleteByUuid(String uuid);

    List<GroupAssignmentDO> listGroupAssignment(Integer limit);
}
