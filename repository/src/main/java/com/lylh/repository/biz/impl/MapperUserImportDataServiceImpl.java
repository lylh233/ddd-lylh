package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperUserImportDataService;
import com.lylh.repository.dao.UserImportDataMapper;
import com.lylh.repository.entity.UserImportDataDO;
import org.springframework.stereotype.Service;

@Service
public class MapperUserImportDataServiceImpl extends ServiceImpl<UserImportDataMapper, UserImportDataDO>
        implements MapperUserImportDataService {
}
