package com.lylh.app.api;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.vo.UserImportTaskVO;
import com.lylh.biz.service.UserImportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户批量导入。文件按内容去重，解析在后台进行，调用方用任务编号查进度。
 */
@RestController
@RequestMapping("/api/user/import")
public class UserImportController {

    private final UserImportService userImportService;

    public UserImportController(UserImportService userImportService) {
        this.userImportService = userImportService;
    }

    /**
     * 导入 csv 或 Excel。第一行表头，其后每行四列：用户名、姓名、密码、用户类型（1 公众用户，2 工作人员）。
     *
     * @param file 上传文件
     * @return 处理中的任务
     */
    @PostMapping
    public CommonResult<UserImportTaskVO> importUsers(@RequestParam("file") MultipartFile file) {
        return ResultUtils.ok(userImportService.importUsers(file));
    }

    /**
     * @param taskId 导入任务编号
     * @return 任务进度
     */
    @GetMapping("/{taskId}")
    public CommonResult<UserImportTaskVO> getTask(@PathVariable("taskId") Long taskId) {
        return ResultUtils.ok(userImportService.getTask(taskId));
    }
}
