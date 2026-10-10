package com.lylh.biz.model.enums;

import com.lylh.repository.entity.UserImportTaskDO;

/**
 * 导入任务状态。取值和任务表 status 字段一致。
 */
public enum UserImportStatusEnum {

    PROCESSING(UserImportTaskDO.STATUS_PROCESSING, "处理中"),
    SUCCESS(UserImportTaskDO.STATUS_SUCCESS, "已完成"),
    FAILED(UserImportTaskDO.STATUS_FAILED, "失败");

    private final String code;
    private final String desc;

    UserImportStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * @param code 库存状态
     * @return 对应枚举，无法识别时返回 null
     */
    public static UserImportStatusEnum parse(String code) {
        for (UserImportStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
