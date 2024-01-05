package com.lylh.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.model.dto.MemberDTO;
import com.lylh.biz.model.vo.MemberVO;

import java.util.List;

public interface MemberService {

    String create(MemberDTO memberDTO);

    void createBatch(List<MemberDTO> memberDTOList);

    void update(MemberDTO memberDTO);

    void updateBatch(List<MemberDTO> memberDTOList);

    void delete(String uuid);

    Page<MemberVO> queryMember(String filter, Integer pageNum, Integer pageSize);

    List<MemberVO> listMember(String filter, Integer limit);

    MemberVO getByUuid(String uuid);

}
