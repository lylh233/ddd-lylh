package com.lylh.biz.service;

import com.lylh.biz.model.dto.OccupationDTO;
import com.lylh.biz.model.vo.OccupationVO;

import java.util.List;

public interface OccupationService {

    String create(OccupationDTO occupationDTO);

    void update(OccupationDTO occupationDTO);

    void delete(String uuid);

    OccupationVO getByUuid(String uuid);

    List<OccupationVO> listOccupation(Integer limit);

}
