package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.biz.model.filters.MemberFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.entity.MemberDO;
import com.lylh.repository.entity.ext.MemberExtDO;

import java.util.List;

public interface MapperMemberService extends IService<MemberDO> {

    MemberDO getByUuid(String uuid);

    void updateMember(MemberDO memberDO, String name, String position, String occupationUuid);

    void deleteByUuid(String uuid);

    List<MemberExtDO> queryMemberList(MemberFilter memberFilter, MapperPage page);

    long queryMemberCount(MemberFilter memberFilter);

    List<MemberExtDO> getMemberList(MemberFilter memberFilter, Integer limit);

    List<MemberExtDO> getMemberWithGroupListByGroupUuids(List<String> groupUuids);
}
