package com.lylh.biz.service.impl;

import com.lylh.biz.service.GroupService;
import com.lylh.repository.biz.MapperGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private MapperGroupService mapperGroupService;
}
