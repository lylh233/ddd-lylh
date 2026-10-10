package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.entity.UserImportTaskDO;

/**
 * 预导入任务读写。是否允许重复导入由 dedup_key 是否占用决定。
 */
public interface MapperUserImportTaskService extends IService<UserImportTaskDO> {

    /**
     * @param dedupKey 文件内容摘要，空摘要不查
     * @return 已占用该文件的任务，没有则返回 null
     */
    UserImportTaskDO getByDedupKey(String dedupKey);

    /**
     * 一批落库后累加进度，调用方可以看到处理中的数量。
     *
     * @param id      任务 id
     * @param total   本批行数
     * @param success 本批成功数
     * @param fail    本批失败数
     */
    void addCounts(Long id, int total, int success, int fail);

    /**
     * 文件全部读完后写成最终数量。用覆盖而不是再累加，避免和批次进度重复计算。
     *
     * @param id      任务 id
     * @param total   总行数
     * @param success 成功数
     * @param fail    失败数
     */
    void markSuccess(Long id, int total, int success, int fail);

    /**
     * 任务级失败。还没有任何行落库时释放 dedup_key，同一文件才能重试。
     *
     * @param id      任务 id
     * @param message 失败原因，会截断到字段长度
     */
    void markFailed(Long id, String message);
}
