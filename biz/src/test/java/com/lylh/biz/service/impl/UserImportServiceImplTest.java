package com.lylh.biz.service.impl;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.lylh.biz.model.vo.UserImportTaskVO;
import com.lylh.biz.service.UserImportWorker;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperUserImportTaskService;
import com.lylh.repository.entity.UserImportTaskDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserImportServiceImplTest {

    @Mock
    private MapperUserImportTaskService taskService;

    @Mock
    private UserImportWorker worker;

    @InjectMocks
    private UserImportServiceImpl userImportService;

    @Test
    void should_reject_when_suffixUnsupported() {
        MockMultipartFile file = new MockMultipartFile("file", "users.txt", "text/plain", "a".getBytes(StandardCharsets.UTF_8));

        BizException exception = catchThrowableOfType(() -> userImportService.importUsers(file), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.IMPORT_FILE_INVALID.getCode());
        verify(taskService, never()).save(any());
    }

    @Test
    void should_reject_when_sameFileAlreadyImported() {
        when(taskService.getByDedupKey(any())).thenReturn(new UserImportTaskDO());
        MockMultipartFile file = csv("users.csv", "username,name,password,user_type\n");

        BizException exception = catchThrowableOfType(() -> userImportService.importUsers(file), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.DUPLICATE_IMPORT_FILE.getCode());
        verify(worker, never()).execute(any(), any(), any());
    }

    @Test
    void should_reject_when_dedupKeyConflicts() {
        when(taskService.save(any())).thenThrow(new DuplicateKeyException("dup"));
        MockMultipartFile file = csv("users.xlsx", "not-empty");

        BizException exception = catchThrowableOfType(() -> userImportService.importUsers(file), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.DUPLICATE_IMPORT_FILE.getCode());
        verify(worker, never()).execute(any(), any(), any());
    }

    @Test
    void should_submitTask_when_fileAccepted() throws Exception {
        when(taskService.save(any())).thenAnswer(invocation -> {
            UserImportTaskDO task = invocation.getArgument(0);
            task.setId(15L);
            return true;
        });
        MockMultipartFile file = csv("a/users.csv", "username,name,password,user_type\nadmin,浩,123456,2\n");

        UserImportTaskVO vo = userImportService.importUsers(file);

        ArgumentCaptor<Path> path = ArgumentCaptor.forClass(Path.class);
        verify(worker).execute(eq(15L), path.capture(), eq(ExcelTypeEnum.CSV));
        assertThat(vo.getId()).isEqualTo(15L);
        assertThat(vo.getFileName()).isEqualTo("users.csv");
        assertThat(vo.getStatus()).isEqualTo("PROCESSING");
        assertThat(Files.exists(path.getValue())).isTrue();
        Files.deleteIfExists(path.getValue());
    }

    @Test
    void should_throw_when_taskMissing() {
        when(taskService.getById(3L)).thenReturn(null);

        BizException exception = catchThrowableOfType(() -> userImportService.getTask(3L), BizException.class);

        assertThat(exception.getCode()).isEqualTo(ResponseCode.IMPORT_TASK_NOT_FOUND.getCode());
    }

    private MockMultipartFile csv(String name, String content) {
        return new MockMultipartFile("file", name, "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }
}
