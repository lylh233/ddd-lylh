package com.lylh.biz.service;

import com.lylh.biz.model.dto.GroupAssignmentDTO;
import com.lylh.biz.model.vo.GroupAssignmentVO;

import java.util.List;

public interface GroupAssignmentService {

    String create(GroupAssignmentDTO groupAssignmentDTO);

    void update(GroupAssignmentDTO groupAssignmentDTO);

    void delete(String uuid);

    GroupAssignmentVO getByUuid(String uuid);

    List<GroupAssignmentVO> listGroupAssignment(Integer limit);
}
