package com.lylh.biz.service.impl;

import com.lylh.biz.service.GroupAssignmentService;
import com.lylh.repository.biz.MapperGroupAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupAssignmentServiceImpl implements GroupAssignmentService {

    @Autowired
    private MapperGroupAssignmentService mapperGroupAssignmentService;

}
