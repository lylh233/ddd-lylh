package com.lylh.biz.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamWithGroupVO extends TeamVO {

    private List<GroupWithMemberVO> groupList;
}
