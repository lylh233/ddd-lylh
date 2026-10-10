package com.lylh.biz.service;

import com.lylh.biz.model.vo.UserImportTaskVO;
import com.lylh.common.model.BizException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户批量导入。同一文件内容只允许有一个进行中或已完成的任务。
 */
public interface UserImportService {

    /**
     * 接收文件并创建导入任务。解析在后台进行，接口只返回任务编号。
     *
     * @param file csv、xls 或 xlsx。第一行是表头，列顺序为用户名、姓名、密码、用户类型
     * @return 刚创建的任务，状态为处理中
     * @throws BizException 文件类型不支持，或同一文件已经导入
     */
    UserImportTaskVO importUsers(MultipartFile file);

    /**
     * @param taskId 任务编号
     * @return 当前进度
     * @throws BizException 任务不存在
     */
    UserImportTaskVO getTask(Long taskId);
}
