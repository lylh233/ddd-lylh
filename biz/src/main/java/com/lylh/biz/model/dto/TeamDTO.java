package com.lylh.biz.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamDTO {

    private String uuid;

    private Integer serialNumber;

    private String description;

    private List<GroupDTO> groupList;

    public TeamDTO() {}

}
