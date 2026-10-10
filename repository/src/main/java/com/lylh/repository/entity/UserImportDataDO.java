package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 校验通过并已写入用户表的导入行。密码只留在用户表，这里不重复保存明文。
 */
@Data
@TableName("user_import_data")
public class UserImportDataDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Integer rowNum;

    private String username;

    private String name;

    private Integer userType;

    private String userUuid;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;
}
