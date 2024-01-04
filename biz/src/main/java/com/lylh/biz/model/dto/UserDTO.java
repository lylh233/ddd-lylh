package com.lylh.biz.model.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserDTO {

    private String uuid;
    @NonNull
    private String name;
    @NonNull
    private String username;
    private String password;
    @NonNull
    private Integer userType;

}
