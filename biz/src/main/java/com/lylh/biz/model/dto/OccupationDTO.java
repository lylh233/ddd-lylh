package com.lylh.biz.model.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class OccupationDTO {

    private String uuid;
    @NonNull
    private String name;

    private String color;

    public OccupationDTO() {}

}
