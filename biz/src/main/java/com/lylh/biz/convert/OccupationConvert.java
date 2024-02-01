package com.lylh.biz.convert;

import com.lylh.biz.model.dto.OccupationDTO;
import com.lylh.biz.model.vo.OccupationVO;
import com.lylh.common.utils.UUIDUtils;
import com.lylh.repository.entity.OccupationDO;

public class OccupationConvert {

    public static OccupationDO toOccupationDO(OccupationDTO occupationDTO) {
        OccupationDO occupationDO = new OccupationDO();
        occupationDO.setUuid(UUIDUtils.simpleUUID());
        occupationDO.setName(occupationDTO.getName());
        occupationDO.setColor(occupationDTO.getColor());
        return occupationDO;
    }

    public static OccupationVO toOccupationVO(OccupationDO occupationDO) {
        OccupationVO occupationVO = new OccupationVO();
        occupationVO.setUuid(occupationDO.getUuid());
        occupationVO.setName(occupationDO.getName());
        occupationVO.setColor(occupationDO.getColor());
        return occupationVO;
    }
}
