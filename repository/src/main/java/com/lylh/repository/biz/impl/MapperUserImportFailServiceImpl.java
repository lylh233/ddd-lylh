package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperUserImportFailService;
import com.lylh.repository.dao.UserImportFailMapper;
import com.lylh.repository.entity.UserImportFailDO;
import org.springframework.stereotype.Service;

@Service
public class MapperUserImportFailServiceImpl extends ServiceImpl<UserImportFailMapper, UserImportFailDO>
        implements MapperUserImportFailService {
}
