package com.lylh.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lylh.repository.biz.model.filters.UserFilter;
import com.lylh.repository.biz.model.page.MapperPage;
import com.lylh.repository.entity.UserDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表访问。列表查询由调用方传入分页参数，XML 负责限制返回行数。
 */
public interface UserMapper extends BaseMapper<UserDO> {

    List<UserDO> queryUserList(@Param("filter") UserFilter filter, @Param("page") MapperPage page);

    long queryUserCount(@Param("filter") UserFilter filter);
}