package com.lylh.repository.biz.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lylh.repository.biz.MapperGroupAssignmentService;
import com.lylh.repository.dao.GroupAssignmentMapper;
import com.lylh.repository.entity.GroupAssignmentDO;
import org.springframework.stereotype.Service;

@Service
public class MapperGroupAssignmentServiceImpl extends ServiceImpl<GroupAssignmentMapper, GroupAssignmentDO> implements MapperGroupAssignmentService {

}
