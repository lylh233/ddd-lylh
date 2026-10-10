package com.lylh.biz.service.impl;

import com.lylh.biz.convert.UserConvert;
import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.excel.ImportBatchResult;
import com.lylh.biz.model.excel.UserImportLimits;
import com.lylh.biz.model.excel.UserImportLine;
import com.lylh.biz.model.excel.UserImportRowChecker;
import com.lylh.biz.model.excel.UserImportRowChecker.CheckOutcome;
import com.lylh.biz.service.UserImportPersistService;
import com.lylh.repository.biz.MapperUserImportDataService;
import com.lylh.repository.biz.MapperUserImportFailService;
import com.lylh.repository.biz.MapperUserImportTaskService;
import com.lylh.repository.biz.MapperUserService;
import com.lylh.repository.entity.UserDO;
import com.lylh.repository.entity.UserImportDataDO;
import com.lylh.repository.entity.UserImportFailDO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 把一批导入行写入用户表、合规表和失败表。一批一个事务，整份百万行文件不放在同一个事务里。
 */
@Service
public class UserImportPersistServiceImpl implements UserImportPersistService {

    private static final String DUPLICATE_IN_FILE = "文件内用户名重复";
    private static final String DUPLICATE_IN_DB = "用户名已存在";
    private static final String PASSWORD_TOO_LONG = "密码加密后超过存储长度";

    private final MapperUserService mapperUserService;
    private final MapperUserImportDataService dataService;
    private final MapperUserImportFailService failService;
    private final MapperUserImportTaskService taskService;

    public UserImportPersistServiceImpl(MapperUserService mapperUserService,
                                        MapperUserImportDataService dataService,
                                        MapperUserImportFailService failService,
                                        MapperUserImportTaskService taskService) {
        this.mapperUserService = mapperUserService;
        this.dataService = dataService;
        this.failService = failService;
        this.taskService = taskService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportBatchResult persist(Long taskId, List<UserImportLine> lines, Set<String> accepted) {
        Classified classified = classify(taskId, lines, accepted);
        save(taskId, classified);
        ImportBatchResult result = new ImportBatchResult();
        result.setSuccessCount(classified.users.size());
        result.setFailCount(classified.fails.size());
        result.setAcceptedUsernames(classified.acceptedUsernames);
        return result;
    }

    private Classified classify(Long taskId, List<UserImportLine> lines, Set<String> accepted) {
        Classified classified = new Classified();
        List<PreparedRow> ready = new ArrayList<>();
        Set<String> batchNames = new HashSet<>();
        for (UserImportLine line : lines) {
            collect(taskId, line, accepted, batchNames, ready, classified.fails);
        }
        Set<String> existing = batchNames.isEmpty()
                ? new HashSet<String>()
                : new HashSet<>(mapperUserService.listExistingUsernames(batchNames));
        long now = System.currentTimeMillis();
        for (PreparedRow prepared : ready) {
            if (existing.contains(prepared.user.getUsername())) {
                classified.fails.add(fail(taskId, prepared.rowNum, prepared.source, DUPLICATE_IN_DB, now));
                continue;
            }
            prepared.user.setCreated(now);
            prepared.user.setLastModified(now);
            classified.users.add(prepared.user);
            classified.dataRows.add(toData(taskId, prepared, now));
            classified.acceptedUsernames.add(prepared.user.getUsername());
        }
        return classified;
    }

    private void collect(Long taskId, UserImportLine line, Set<String> accepted, Set<String> batchNames,
                         List<PreparedRow> ready, List<UserImportFailDO> fails) {
        CheckOutcome outcome = UserImportRowChecker.check(line.getRow());
        if (outcome.isSkipped()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!outcome.isPassed()) {
            fails.add(fail(taskId, line.getRowNum(), UserImportRowChecker.sourceOf(line.getRow()), outcome.getReason(), now));
            return;
        }
        UserDO user = toUser(outcome, line.getRow().getPassword());
        if (user.getPassword() == null || user.getPassword().length() > UserImportRowChecker.MAX_PASSWORD_CIPHER_LENGTH) {
            fails.add(fail(taskId, line.getRowNum(), UserImportRowChecker.sourceOf(line.getRow()), PASSWORD_TOO_LONG, now));
            return;
        }
        if (accepted.contains(user.getUsername()) || !batchNames.add(user.getUsername())) {
            fails.add(fail(taskId, line.getRowNum(), UserImportRowChecker.sourceOf(line.getRow()), DUPLICATE_IN_FILE, now));
            return;
        }
        ready.add(new PreparedRow(line.getRowNum(), user, UserImportRowChecker.sourceOf(line.getRow())));
    }

    private void save(Long taskId, Classified classified) {
        try {
            if (!CollectionUtils.isEmpty(classified.users)) {
                mapperUserService.saveBatch(classified.users, UserImportLimits.BATCH_SIZE);
                dataService.saveBatch(classified.dataRows, UserImportLimits.BATCH_SIZE);
            }
            if (!CollectionUtils.isEmpty(classified.fails)) {
                failService.saveBatch(classified.fails, UserImportLimits.BATCH_SIZE);
            }
            int batchTotal = classified.users.size() + classified.fails.size();
            taskService.addCounts(taskId, batchTotal, classified.users.size(), classified.fails.size());
        } catch (DuplicateKeyException ex) {
            throw new IllegalStateException("批量写入用户失败", ex);
        }
    }

    private UserDO toUser(CheckOutcome outcome, String password) {
        UserDTO dto = new UserDTO();
        dto.setUsername(outcome.getUsername());
        dto.setName(outcome.getName());
        dto.setPassword(password);
        dto.setUserType(outcome.getUserType());
        return UserConvert.toUserDO(dto);
    }

    private UserImportDataDO toData(Long taskId, PreparedRow prepared, long now) {
        UserImportDataDO data = new UserImportDataDO();
        data.setTaskId(taskId);
        data.setRowNum(prepared.rowNum);
        data.setUsername(prepared.user.getUsername());
        data.setName(prepared.user.getName());
        data.setUserType(prepared.user.getUserType());
        data.setUserUuid(prepared.user.getUuid());
        data.setCreated(now);
        data.setLastModified(now);
        return data;
    }

    private UserImportFailDO fail(Long taskId, int rowNum, String source, String reason, long now) {
        UserImportFailDO fail = new UserImportFailDO();
        fail.setTaskId(taskId);
        fail.setRowNum(rowNum);
        fail.setSourceContent(source);
        fail.setReason(reason);
        fail.setCreated(now);
        fail.setLastModified(now);
        return fail;
    }

    private static final class Classified {
        private final List<UserDO> users = new ArrayList<>();
        private final List<UserImportDataDO> dataRows = new ArrayList<>();
        private final List<UserImportFailDO> fails = new ArrayList<>();
        private final List<String> acceptedUsernames = new ArrayList<>();
    }

    private static final class PreparedRow {
        private final int rowNum;
        private final UserDO user;
        private final String source;

        private PreparedRow(int rowNum, UserDO user, String source) {
            this.rowNum = rowNum;
            this.user = user;
            this.source = source;
        }
    }
}
