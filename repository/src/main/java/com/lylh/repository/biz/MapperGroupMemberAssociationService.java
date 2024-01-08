package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.entity.GroupMemberAssociationDO;

public interface MapperGroupMemberAssociationService extends IService<GroupMemberAssociationDO> {

    void deleteByMemberUuid(String memberUuid);

    void deleteByGroupUuid(String groupUuid);
}
