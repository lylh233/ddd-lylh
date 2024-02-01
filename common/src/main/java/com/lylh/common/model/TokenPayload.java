package com.lylh.common.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class TokenPayload implements Serializable {

    private String uuid;
    private String name;
    private String username;
    private Integer userType;

}
