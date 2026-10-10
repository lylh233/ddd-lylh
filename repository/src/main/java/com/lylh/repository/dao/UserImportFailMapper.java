package com.lylh.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lylh.repository.entity.UserImportFailDO;

/**
 * 导入失败行。只按任务归档，不参与用户表写入。
 */
public interface UserImportFailMapper extends BaseMapper<UserImportFailDO> {
}
