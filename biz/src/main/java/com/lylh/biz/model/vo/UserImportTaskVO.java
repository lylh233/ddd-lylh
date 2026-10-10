package com.lylh.biz.model.vo;

import lombok.Data;

/**
 * 导入任务进度。不返回文件摘要和临时路径。
 */
@Data
public class UserImportTaskVO {

    private Long id;

    private String fileName;

    private String status;

    private String statusDesc;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private String errorMessage;
}
