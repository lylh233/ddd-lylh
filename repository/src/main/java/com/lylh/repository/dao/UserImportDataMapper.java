package com.lylh.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lylh.repository.entity.UserImportDataDO;

/**
 * 合规导入行。按任务查询时走 task_id 索引，不和失败表做连接。
 */
public interface UserImportDataMapper extends BaseMapper<UserImportDataDO> {
}
