package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 小队成员关联
 *
 * @author lylh
 * @version 1.0.0
 * @since 2024-01-22
 */
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
