package com.lylh.biz.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserQuery {

    private String name;

    @JsonProperty("user_type")
    private Integer userType;

}
