package com.lylh.biz.service;

import com.lylh.biz.model.dto.GroupDTO;
import com.lylh.biz.model.vo.GroupVO;
import com.lylh.biz.model.vo.GroupWithMemberVO;

import java.util.List;

public interface GroupService {

    String create(GroupDTO groupDTO);

    void createBatch(List<GroupDTO> groupDTOList);

    void update(GroupDTO groupDTO);

    void updateBatch(List<GroupDTO> groupDTOList);

    void delete(String uuid);

}
