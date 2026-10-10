package com.lylh.biz.service.impl;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.lylh.biz.convert.UserImportConvert;
import com.lylh.biz.model.enums.UserImportStatusEnum;
import com.lylh.biz.model.vo.UserImportTaskVO;
import com.lylh.biz.service.UserImportService;
import com.lylh.biz.service.UserImportWorker;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperUserImportTaskService;
import com.lylh.repository.entity.UserImportTaskDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 先按文件内容占用导入名额，再把临时文件交给后台线程。重复文件在接口里直接拒绝。
 */
@Slf4j
@Service
public class UserImportServiceImpl implements UserImportService {

    private static final int MAX_FILE_NAME_LENGTH = 255;

    private final MapperUserImportTaskService taskService;
    private final UserImportWorker worker;

    public UserImportServiceImpl(MapperUserImportTaskService taskService, UserImportWorker worker) {
        this.taskService = taskService;
        this.worker = worker;
    }

    @Override
    public UserImportTaskVO importUsers(MultipartFile file) {
        String fileName = resolveFileName(file);
        ExcelTypeEnum excelType = resolveExcelType(fileName);
        if (file.getSize() == 0) {
            throw new BizException(ResponseCode.IMPORT_FILE_INVALID);
        }
        Path temp = null;
        boolean submitted = false;
        try {
            CopiedFile copied = copyAndHash(file, excelType.getValue());
            temp = copied.path;
            if (taskService.getByDedupKey(copied.hash) != null) {
                log.warn("拒绝重复导入, fileName={}", fileName);
                throw new BizException(ResponseCode.DUPLICATE_IMPORT_FILE);
            }
            UserImportTaskDO task = newTask(fileName, copied.hash);
            try {
                taskService.save(task);
            } catch (DuplicateKeyException ex) {
                log.warn("拒绝重复导入, fileName={}", fileName);
                throw new BizException(ResponseCode.DUPLICATE_IMPORT_FILE, ex);
            }
            worker.execute(task.getId(), temp, excelType);
            submitted = true;
            return UserImportConvert.toTaskVO(task);
        } catch (IOException ex) {
            throw new BizException(ResponseCode.FAIL, ex);
        } finally {
            if (!submitted) {
                deleteQuietly(temp);
            }
        }
    }

    @Override
    public UserImportTaskVO getTask(Long taskId) {
        UserImportTaskDO task = taskService.getById(taskId);
        if (task == null) {
            throw new BizException(ResponseCode.IMPORT_TASK_NOT_FOUND);
        }
        return UserImportConvert.toTaskVO(task);
    }

    private UserImportTaskDO newTask(String fileName, String hash) {
        UserImportTaskDO task = new UserImportTaskDO();
        task.setFileName(fileName);
        task.setFileHash(hash);
        task.setDedupKey(hash);
        task.setStatus(UserImportStatusEnum.PROCESSING.getCode());
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setCreated(System.currentTimeMillis());
        task.setLastModified(task.getCreated());
        return task;
    }

    private String resolveFileName(MultipartFile file) {
        if (file == null || !StringUtils.hasText(file.getOriginalFilename())) {
            throw new BizException(ResponseCode.IMPORT_FILE_INVALID);
        }
        String original = file.getOriginalFilename();
        int slash = Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\'));
        String fileName = slash >= 0 ? original.substring(slash + 1) : original;
        if (fileName.length() <= MAX_FILE_NAME_LENGTH) {
            return fileName;
        }
        return fileName.substring(fileName.length() - MAX_FILE_NAME_LENGTH);
    }

    private ExcelTypeEnum resolveExcelType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".csv")) {
            return ExcelTypeEnum.CSV;
        }
        if (lower.endsWith(".xlsx")) {
            return ExcelTypeEnum.XLSX;
        }
        if (lower.endsWith(".xls")) {
            return ExcelTypeEnum.XLS;
        }
        throw new BizException(ResponseCode.IMPORT_FILE_INVALID);
    }

    private CopiedFile copyAndHash(MultipartFile file, String suffix) throws IOException {
        MessageDigest digest = sha256();
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"), "user-import");
        Files.createDirectories(dir);
        Path temp = Files.createTempFile(dir, "import-", suffix);
        try (InputStream in = file.getInputStream(); OutputStream out = Files.newOutputStream(temp)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
                out.write(buffer, 0, read);
            }
        } catch (IOException ex) {
            deleteQuietly(temp);
            throw ex;
        }
        return new CopiedFile(temp, toHex(digest.digest()));
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value & 0xff));
        }
        return builder.toString();
    }

    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.warn("删除导入临时文件失败, path={}", file.getFileName());
        }
    }

    private static final class CopiedFile {
        private final Path path;
        private final String hash;

        private CopiedFile(Path path, String hash) {
            this.path = path;
            this.hash = hash;
        }
    }
}
