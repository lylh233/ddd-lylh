package com.lylh.biz.service.impl;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.lylh.biz.model.excel.ImportBatchResult;
import com.lylh.biz.service.UserImportPersistService;
import com.lylh.repository.biz.MapperUserImportTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserImportWorkerTest {

    @Mock
    private UserImportPersistService persistService;

    @Mock
    private MapperUserImportTaskService taskService;

    @InjectMocks
    private UserImportWorkerImpl worker;

    @Test
    void should_readRowsAndDeleteTempFile_when_csvFinished() throws Exception {
        when(persistService.persist(anyLong(), anyList(), anySet())).thenAnswer(invocation -> {
            List<?> lines = invocation.getArgument(1);
            ImportBatchResult result = new ImportBatchResult();
            result.setSuccessCount(lines.size());
            result.setFailCount(0);
            return result;
        });
        Path file = Files.createTempFile("user-import-", ".csv");
        Files.write(file, "username,name,password,user_type\nadmin,浩,123456,2\n,浩,123456,1\n".getBytes(StandardCharsets.UTF_8));

        worker.execute(21L, file, ExcelTypeEnum.CSV);

        verify(taskService).markSuccess(21L, 2, 2, 0);
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void should_markFailedAndDeleteFile_when_contentUnreadable() throws Exception {
        Path file = Files.createTempFile("user-import-", ".xlsx");
        Files.write(file, "not-an-excel".getBytes(StandardCharsets.UTF_8));

        worker.execute(22L, file, ExcelTypeEnum.XLSX);

        verify(taskService).markFailed(org.mockito.ArgumentMatchers.eq(22L), org.mockito.ArgumentMatchers.anyString());
        assertThat(Files.exists(file)).isFalse();
    }
}
