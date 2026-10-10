package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 未通过校验的导入行。保留原始单元格，方便按原因修正后重新提交。
 */
@Data
@TableName("user_import_fail")
public class UserImportFailDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Integer rowNum;

    private String sourceContent;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;
}
