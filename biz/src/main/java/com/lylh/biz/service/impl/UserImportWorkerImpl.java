package com.lylh.biz.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.lylh.biz.model.excel.ImportBatchResult;
import com.lylh.biz.model.excel.UserImportBatchHandler;
import com.lylh.biz.model.excel.UserImportLine;
import com.lylh.biz.model.excel.UserImportReadListener;
import com.lylh.biz.model.excel.UserImportRow;
import com.lylh.biz.service.UserImportPersistService;
import com.lylh.biz.service.UserImportWorker;
import com.lylh.repository.biz.MapperUserImportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * 在独立线程里读文件。请求线程只负责收文件和建任务，避免百万行把接口拖死。
 */
@Slf4j
@Service
public class UserImportWorkerImpl implements UserImportWorker {

    private final UserImportPersistService persistService;
    private final MapperUserImportTaskService taskService;

    public UserImportWorkerImpl(UserImportPersistService persistService, MapperUserImportTaskService taskService) {
        this.persistService = persistService;
        this.taskService = taskService;
    }

    @Override
    public void execute(Long taskId, Path file, ExcelTypeEnum excelType) {
        try {
            log.info("开始导入用户, taskId={}", taskId);
            UserImportReadListener listener = new UserImportReadListener(taskId, new BatchHandler());
            ExcelReaderBuilder builder = EasyExcel.read(file.toFile(), UserImportRow.class, listener)
                    .excelType(excelType)
                    .headRowNumber(1)
                    .autoTrim(false);
            if (excelType == ExcelTypeEnum.CSV) {
                builder.charset(StandardCharsets.UTF_8);
            }
            builder.sheet().doRead();
            log.info("导入用户完成, taskId={}", taskId);
        } catch (Exception ex) {
            log.error("导入用户失败, taskId={}", taskId, ex);
            taskService.markFailed(taskId, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        } finally {
            deleteQuietly(file);
        }
    }

    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (Exception ex) {
            log.warn("删除导入临时文件失败, path={}", file.getFileName(), ex);
        }
    }

    private final class BatchHandler implements UserImportBatchHandler {

        @Override
        public ImportBatchResult persist(Long taskId, List<UserImportLine> lines, Set<String> accepted) {
            return persistService.persist(taskId, lines, accepted);
        }

        @Override
        public void finish(Long taskId, int total, int success, int fail) {
            taskService.markSuccess(taskId, total, success, fail);
        }
    }
}
