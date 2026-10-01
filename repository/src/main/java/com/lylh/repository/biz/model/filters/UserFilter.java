package com.lylh.repository.biz.model.filters;

import lombok.Data;

/**
 * 用户列表筛选。类型字段名是 useType，SQL 绑定必须使用同一属性。
 */
@Data
public class UserFilter {

    private String name;
    private Integer useType;

}
