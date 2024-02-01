package com.lylh.repository.biz.model.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MemberFilter {

    private String name;
    private String position;
    @JsonProperty("occupation_uuid")
    private String occupationUuid;
    @JsonProperty("key_name")
    private String keyName;

}
