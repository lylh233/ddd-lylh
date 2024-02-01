package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 小队
 *
 * @author lylh
 * @version 1.0.0
 * @since 2024-01-22
 */
@Data
@TableName("group")
public class GroupDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;

    private Integer serialNumber;

    private String teamUuid;

    private String groupAssignmentUuid;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;
}
