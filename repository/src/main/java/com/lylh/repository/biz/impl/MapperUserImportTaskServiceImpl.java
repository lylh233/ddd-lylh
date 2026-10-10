package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperUserImportTaskService;
import com.lylh.repository.dao.UserImportTaskMapper;
import com.lylh.repository.entity.UserImportTaskDO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 预导入任务持久化。失败且尚未写入任何行时清空占用键，避免坏文件永远不能重传。
 */
@Service
public class MapperUserImportTaskServiceImpl extends ServiceImpl<UserImportTaskMapper, UserImportTaskDO>
        implements MapperUserImportTaskService {

    private static final int MAX_ERROR_LENGTH = 500;

    @Override
    public UserImportTaskDO getByDedupKey(String dedupKey) {
        if (!StringUtils.hasText(dedupKey)) {
            return null;
        }
        return baseMapper.selectOne(new LambdaQueryWrapper<UserImportTaskDO>()
                .eq(UserImportTaskDO::getDedupKey, dedupKey));
    }

    @Override
    public void addCounts(Long id, int total, int success, int fail) {
        baseMapper.addCounts(id, total, success, fail, System.currentTimeMillis());
    }

    @Override
    public void markSuccess(Long id, int total, int success, int fail) {
        update(new LambdaUpdateWrapper<UserImportTaskDO>()
                .set(UserImportTaskDO::getStatus, UserImportTaskDO.STATUS_SUCCESS)
                .set(UserImportTaskDO::getTotalCount, total)
                .set(UserImportTaskDO::getSuccessCount, success)
                .set(UserImportTaskDO::getFailCount, fail)
                .set(UserImportTaskDO::getLastModified, System.currentTimeMillis())
                .eq(UserImportTaskDO::getId, id));
    }

    @Override
    public void markFailed(Long id, String message) {
        UserImportTaskDO task = getById(id);
        if (task == null) {
            return;
        }
        LambdaUpdateWrapper<UserImportTaskDO> wrapper = new LambdaUpdateWrapper<UserImportTaskDO>()
                .set(UserImportTaskDO::getStatus, UserImportTaskDO.STATUS_FAILED)
                .set(UserImportTaskDO::getErrorMessage, truncate(message))
                .set(UserImportTaskDO::getLastModified, System.currentTimeMillis())
                .eq(UserImportTaskDO::getId, id);
        int success = task.getSuccessCount() == null ? 0 : task.getSuccessCount();
        int fail = task.getFailCount() == null ? 0 : task.getFailCount();
        if (success == 0 && fail == 0) {
            wrapper.set(UserImportTaskDO::getDedupKey, null);
        }
        update(wrapper);
    }

    private String truncate(String message) {
        if (!StringUtils.hasText(message)) {
            return "导入失败";
        }
        if (message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_LENGTH);
    }
}
