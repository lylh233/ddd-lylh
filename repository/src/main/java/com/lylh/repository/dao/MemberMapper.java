package com.lylh.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lylh.repository.biz.model.filters.MemberFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.entity.MemberDO;
import com.lylh.repository.entity.ext.MemberExtDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemberMapper extends BaseMapper<MemberDO> {

    List<MemberExtDO> queryMemberList(@Param("filter") MemberFilter filter, @Param("page") MapperPage page);

    long queryMemberCount(@Param("filter") MemberFilter filter);

    List<MemberExtDO> getMemberList(@Param("filter") MemberFilter filter, @Param("limit") Integer limit);

    List<MemberExtDO> getMemberWithGroupListByGroupUuids(@Param("groupUuids") List<String> groupUuids);
}
