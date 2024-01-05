package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("group_member_association")
public class GroupMemberAssociationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer serialNumber;

    private String groupUuid;

    private String memberUuid;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;

}
