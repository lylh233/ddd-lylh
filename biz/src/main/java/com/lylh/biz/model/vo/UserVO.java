package com.lylh.biz.model.vo;

import lombok.Data;

@Data
public class UserVO {

    private String uuid;

    private String username;

    private String name;

    private Integer userType;

    private String userTypeDesc;

    private Long created;

    private Long lastModified;

}
