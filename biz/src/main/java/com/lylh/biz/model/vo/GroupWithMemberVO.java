package com.lylh.biz.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupWithMemberVO {

    private String uuid;

    private Integer serialNumber;

    private String teamUuid;

    private String GroupAssignmentName;

    private List<MemberVO> memberList;
}
