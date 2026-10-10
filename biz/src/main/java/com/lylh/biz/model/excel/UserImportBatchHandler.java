package com.lylh.biz.model.excel;

import java.util.List;
import java.util.Set;

/**
 * 监听器把一批行交回业务层。定义在模型侧，避免 Excel 监听反过来依赖 Service。
 */
public interface UserImportBatchHandler {

    /**
     * @param taskId   预导入任务
     * @param lines    本批非空行
     * @param accepted 本文件已经准备入库的用户名
     * @return 本批写入结果
     */
    ImportBatchResult persist(Long taskId, List<UserImportLine> lines, Set<String> accepted);

    /**
     * 文件读完后回写最终数量。
     *
     * @param taskId  任务编号
     * @param total   总行数
     * @param success 成功数
     * @param fail    失败数
     */
    void finish(Long taskId, int total, int success, int fail);
}
