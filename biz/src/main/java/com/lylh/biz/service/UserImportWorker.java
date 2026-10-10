package com.lylh.biz.service;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.lylh.biz.model.excel.UserImportExecutorNames;
import org.springframework.scheduling.annotation.Async;

import java.nio.file.Path;

/**
 * 在独立线程里读导入文件。
 */
public interface UserImportWorker {

    /**
     * @param taskId    已创建的任务
     * @param file      服务端临时文件，读完必须删除
     * @param excelType csv、xls 或 xlsx
     */
    @Async(UserImportExecutorNames.EXECUTOR)
    void execute(Long taskId, Path file, ExcelTypeEnum excelType);
}
