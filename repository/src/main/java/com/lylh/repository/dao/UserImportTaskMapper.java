package com.lylh.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lylh.repository.entity.UserImportTaskDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 预导入任务。进度用一条累加语句更新，避免把整次导入包进一个长事务。
 */
public interface UserImportTaskMapper extends BaseMapper<UserImportTaskDO> {

    @Update("UPDATE user_import_task SET total_count = total_count + #{total}, "
            + "success_count = success_count + #{success}, fail_count = fail_count + #{fail}, "
            + "last_modified = #{now} WHERE id = #{id}")
    int addCounts(@Param("id") Long id, @Param("total") int total, @Param("success") int success,
                  @Param("fail") int fail, @Param("now") long now);
}
