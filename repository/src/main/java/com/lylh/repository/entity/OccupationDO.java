package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("occupation")
public class OccupationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;

}
