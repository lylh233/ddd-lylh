package com.lylh.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Objects;

/**
 * 用户表行。相等性只看 uuid，同步比对时同一用户不会因为其他字段不同被当成两条。
 */
@Data
@TableName("user")
public class UserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;

    private String username;

    private String password;

    private String name;

    private Integer userType;

    @TableField(fill = FieldFill.INSERT)
    private Long created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long lastModified;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDO that = (UserDO) o;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }

}