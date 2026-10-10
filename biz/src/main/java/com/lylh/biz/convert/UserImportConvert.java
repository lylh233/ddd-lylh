package com.lylh.biz.convert;

import com.lylh.biz.model.enums.UserImportStatusEnum;
import com.lylh.biz.model.vo.UserImportTaskVO;
import com.lylh.repository.entity.UserImportTaskDO;

/**
 * 导入任务转成接口视图。
 */
public final class UserImportConvert {

    private UserImportConvert() {
    }

    /**
     * @param task 任务行
     * @return 给调用方看的进度
     */
    public static UserImportTaskVO toTaskVO(UserImportTaskDO task) {
        UserImportTaskVO vo = new UserImportTaskVO();
        vo.setId(task.getId());
        vo.setFileName(task.getFileName());
        vo.setStatus(task.getStatus());
        UserImportStatusEnum status = UserImportStatusEnum.parse(task.getStatus());
        vo.setStatusDesc(status == null ? task.getStatus() : status.getDesc());
        vo.setTotalCount(task.getTotalCount());
        vo.setSuccessCount(task.getSuccessCount());
        vo.setFailCount(task.getFailCount());
        vo.setErrorMessage(task.getErrorMessage());
        return vo;
    }
}
