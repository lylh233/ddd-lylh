package com.lylh.biz.model.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * 一批导入的写入结果。acceptedUsernames 要在事务提交后才加入文件级去重集合。
 */
public class ImportBatchResult {

    private int successCount;
    private int failCount;
    private List<String> acceptedUsernames = new ArrayList<>();

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<String> getAcceptedUsernames() {
        return acceptedUsernames;
    }

    public void setAcceptedUsernames(List<String> acceptedUsernames) {
        this.acceptedUsernames = acceptedUsernames;
    }
}
