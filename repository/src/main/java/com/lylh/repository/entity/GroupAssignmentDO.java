package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 小队任务
 *
 * @author lylh
 * @version 1.0.0
 * @since 2024-01-22
 */
@Data
@TableName("group_assignment")
public class GroupAssignmentDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;

    private String name;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;
}
