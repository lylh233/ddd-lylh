package com.lylh.biz.service;

import com.lylh.biz.model.excel.ImportBatchResult;
import com.lylh.biz.model.excel.UserImportLine;

import java.util.List;
import java.util.Set;

/**
 * 把一批导入行写入用户表、合规表和失败表。
 */
public interface UserImportPersistService {

    /**
     * @param taskId   预导入任务
     * @param lines    本批行，调用方已去掉空行
     * @param accepted 本文件已经准备入库的用户名
     * @return 本批成功数、失败数，以及新占用的用户名
     */
    ImportBatchResult persist(Long taskId, List<UserImportLine> lines, Set<String> accepted);
}
