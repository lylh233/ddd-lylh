package com.lylh.biz.service.impl;

import com.lylh.biz.model.excel.ImportBatchResult;
import com.lylh.biz.model.excel.UserImportLimits;
import com.lylh.biz.model.excel.UserImportLine;
import com.lylh.biz.model.excel.UserImportRow;
import com.lylh.repository.biz.MapperUserImportDataService;
import com.lylh.repository.biz.MapperUserImportFailService;
import com.lylh.repository.biz.MapperUserImportTaskService;
import com.lylh.repository.biz.MapperUserService;
import com.lylh.repository.entity.UserDO;
import com.lylh.repository.entity.UserImportDataDO;
import com.lylh.repository.entity.UserImportFailDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserImportPersistServiceTest {

    @Mock
    private MapperUserService mapperUserService;

    @Mock
    private MapperUserImportDataService dataService;

    @Mock
    private MapperUserImportFailService failService;

    @Mock
    private MapperUserImportTaskService taskService;

    @InjectMocks
    private UserImportPersistServiceImpl persistService;

    @Test
    void should_saveUser_when_rowValid() {
        when(mapperUserService.listExistingUsernames(anyCollection())).thenReturn(Collections.emptyList());

        ImportBatchResult result = persistService.persist(8L, Collections.singletonList(line(2, "admin", "123456", "2")),
                new HashSet<String>());

        ArgumentCaptor<List<UserDO>> users = listCaptor();
        verify(mapperUserService).saveBatch(users.capture(), eq(UserImportLimits.BATCH_SIZE));
        assertThat(users.getValue()).hasSize(1);
        assertThat(users.getValue().get(0).getUsername()).isEqualTo("admin");
        assertThat(users.getValue().get(0).getPassword()).isNotEqualTo("123456");
        ArgumentCaptor<List<UserImportDataDO>> data = listCaptor();
        verify(dataService).saveBatch(data.capture(), eq(UserImportLimits.BATCH_SIZE));
        assertThat(data.getValue().get(0).getUserUuid()).isEqualTo(users.getValue().get(0).getUuid());
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getAcceptedUsernames()).containsExactly("admin");
        verify(failService, never()).saveBatch(anyCollection(), anyInt());
    }

    @Test
    void should_recordFail_when_usernameAlreadyImportedInFile() {
        Set<String> accepted = new HashSet<String>();
        accepted.add("admin");

        ImportBatchResult result = persistService.persist(8L, Collections.singletonList(line(3, "admin", "123456", "1")),
                accepted);

        ArgumentCaptor<List<UserImportFailDO>> fails = listCaptor();
        verify(failService).saveBatch(fails.capture(), eq(UserImportLimits.BATCH_SIZE));
        assertThat(fails.getValue().get(0).getReason()).isEqualTo("文件内用户名重复");
        assertThat(result.getSuccessCount()).isZero();
        verify(mapperUserService, never()).saveBatch(anyCollection(), anyInt());
    }

    @Test
    void should_recordFail_when_usernameExistsInDatabase() {
        when(mapperUserService.listExistingUsernames(anyCollection())).thenReturn(Collections.singletonList("admin"));

        ImportBatchResult result = persistService.persist(8L, Collections.singletonList(line(4, "admin", "123456", "1")),
                new HashSet<String>());

        ArgumentCaptor<List<UserImportFailDO>> fails = listCaptor();
        verify(failService).saveBatch(fails.capture(), eq(UserImportLimits.BATCH_SIZE));
        assertThat(fails.getValue().get(0).getReason()).isEqualTo("用户名已存在");
        assertThat(fails.getValue().get(0).getSourceContent()).contains("admin");
        assertThat(result.getFailCount()).isEqualTo(1);
        verify(taskService).addCounts(eq(8L), eq(1), eq(0), eq(1));
    }

    @Test
    void should_recordFail_when_passwordCipherTooLong() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            password.append('p');
        }

        ImportBatchResult result = persistService.persist(8L,
                Collections.singletonList(line(5, "admin", password.toString(), "1")), new HashSet<String>());

        ArgumentCaptor<List<UserImportFailDO>> fails = listCaptor();
        verify(failService).saveBatch(fails.capture(), anyInt());
        assertThat(fails.getValue().get(0).getReason()).isEqualTo("密码加密后超过存储长度");
        assertThat(result.getAcceptedUsernames()).isEmpty();
        verify(mapperUserService, never()).listExistingUsernames(anyCollection());
    }

    private UserImportLine line(int rowNum, String username, String password, String userType) {
        UserImportRow row = new UserImportRow();
        row.setUsername(username);
        row.setName("浩");
        row.setPassword(password);
        row.setUserType(userType);
        return new UserImportLine(rowNum, row);
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentCaptor<List<T>> listCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
