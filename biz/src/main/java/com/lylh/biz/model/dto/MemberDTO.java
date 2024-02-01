package com.lylh.biz.model.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class MemberDTO {

    private String uuid;
    @NonNull
    private String name;

    private String position;
    @NonNull
    private String occupationUuid;

    public MemberDTO() {}
}
