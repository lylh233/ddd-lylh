package com.lylh.biz.model.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EasyExcel 的行监听。只攒一批就落库，不把整份文件留在内存里。
 */
public class UserImportReadListener implements ReadListener<UserImportRow> {

    private final Long taskId;
    private final UserImportBatchHandler batchHandler;
    private final List<UserImportLine> buffer = new ArrayList<>(UserImportLimits.BATCH_SIZE);
    private final Set<String> acceptedUsernames = new HashSet<>();
    private int total;
    private int success;
    private int fail;

    public UserImportReadListener(Long taskId, UserImportBatchHandler batchHandler) {
        this.taskId = taskId;
        this.batchHandler = batchHandler;
    }

    @Override
    public void invoke(UserImportRow row, AnalysisContext context) {
        if (UserImportRowChecker.isBlank(row)) {
            return;
        }
        total++;
        int rowNum = context.readRowHolder().getRowIndex() + 1;
        buffer.add(new UserImportLine(rowNum, row));
        if (buffer.size() >= UserImportLimits.BATCH_SIZE) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        flush();
        batchHandler.finish(taskId, total, success, fail);
    }

    private void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        ImportBatchResult result = batchHandler.persist(taskId, buffer, acceptedUsernames);
        acceptedUsernames.addAll(result.getAcceptedUsernames());
        success += result.getSuccessCount();
        fail += result.getFailCount();
        buffer.clear();
    }
}
