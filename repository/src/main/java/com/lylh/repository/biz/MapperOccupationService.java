package com.lylh.repository.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lylh.repository.entity.OccupationDO;

import java.util.List;

public interface MapperOccupationService extends IService<OccupationDO> {

    List<OccupationDO> getByUuids(List<String> uuids);

    OccupationDO getByUuid(String uuid);

    void updateOccupation(OccupationDO occupationDO, String name);

    void deleteByUuid(String uuid);

    List<OccupationDO> listOccupation(Integer limit);
}
