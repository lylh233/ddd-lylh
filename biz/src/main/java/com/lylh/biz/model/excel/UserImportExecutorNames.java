package com.lylh.biz.model.excel;

/**
 * 导入线程池的 bean 名称。异步方法必须走这个池，不能在请求线程里读完百万行。
 */
public final class UserImportExecutorNames {

    public static final String EXECUTOR = "userImportExecutor";

    private UserImportExecutorNames() {
    }
}
