package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 预导入任务。dedup_key 有值时表示这份文件内容已经占用，用来挡住重复导入。
 */
@Data
@TableName("user_import_task")
public class UserImportTaskDO {

    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String fileHash;

    private String dedupKey;

    private String status;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;
}
