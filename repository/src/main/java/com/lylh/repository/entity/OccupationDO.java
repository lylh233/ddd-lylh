package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 职业
 *
 * @author lylh
 * @version 1.0.0
 * @since 2024-01-22
 */
@Data
@TableName("occupation")
public class OccupationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;

    private String name;

    private String color;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;

}
