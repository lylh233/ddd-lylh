package com.lylh.repository.biz.model.filters;

import lombok.Data;

@Data
public class MemberFilter {

    private String name;
    private String position;
    private String occupationUuid;
    private String keyName;

}
