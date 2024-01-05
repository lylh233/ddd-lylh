package com.lylh.biz.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.convert.MemberConvert;
import com.lylh.biz.model.common.ServiceUtils;
import com.lylh.biz.model.dto.MemberDTO;
import com.lylh.biz.model.vo.MemberVO;
import com.lylh.biz.service.MemberService;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperGroupMemberAssociationService;
import com.lylh.repository.biz.MapperMemberService;
import com.lylh.repository.biz.MapperOccupationService;
import com.lylh.repository.biz.model.filters.MemberFilter;
import com.lylh.repository.entity.MemberDO;
import com.lylh.repository.entity.ext.MemberExtDO;
import com.lylh.repository.entity.OccupationDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MapperMemberService mapperMemberService;

    @Autowired
    private MapperGroupMemberAssociationService mapperGroupMemberAssociationService;

    @Autowired
    private MapperOccupationService mapperOccupationService;

    @Override
    public String create(MemberDTO memberDTO) {
        MemberDO memberDO = MemberConvert.toMemberDO(memberDTO);
        mapperMemberService.save(memberDO);
        return memberDO.getUuid();
    }

    @Override
    public void createBatch(List<MemberDTO> memberDTOList) {
        List<MemberDO> memberDOList = memberDTOList.stream()
                .map(MemberConvert::toMemberDO).collect(Collectors.toList());
        mapperMemberService.saveBatch(memberDOList);
    }

    @Override
    public void update(MemberDTO memberDTO) {
        MemberDO memberDO = mapperMemberService.getByUuid(memberDTO.getUuid());
        if (memberDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", memberDTO.getUuid(), ">");
        }
        mapperMemberService.updateMember(memberDO, memberDTO.getName(), memberDTO.getPosition(), memberDTO.getOccupationUuid());
    }

    @Override
    public void updateBatch(List<MemberDTO> memberDTOList) {
        memberDTOList.forEach(this::update);
    }

    @Override
    public void delete(String uuid) {
        mapperMemberService.deleteByUuid(uuid);
        mapperGroupMemberAssociationService.deleteByMemberUuid(uuid);
    }

    @Override
    public Page<MemberVO> queryMember(String filter, Integer pageNum, Integer pageSize) {
        MemberFilter memberFilter = JSON.parseObject(filter, new TypeReference<MemberFilter>() {});
        Page<MemberVO> page = ServiceUtils.toPage(pageNum, pageSize);
        List<MemberExtDO> memberExtDOList = mapperMemberService.queryMemberList(memberFilter, ServiceUtils.toMapperPage(page));
        if (CollectionUtils.isEmpty(memberExtDOList)) {
            return page;

        }
        long count = mapperMemberService.queryMemberCount(memberFilter);
        List<MemberVO> memberVOList = memberExtDOList.stream()
                .map(MemberConvert::toMemberVO).collect(Collectors.toList());
        return ServiceUtils.getPage(page, memberVOList, count);
    }

    @Override
    public List<MemberVO> listMember(String filter, Integer limit) {
        MemberFilter memberFilter = JSON.parseObject(filter, new TypeReference<MemberFilter>() {});
        List<MemberExtDO> memberExtDOList = mapperMemberService.getMemberList(memberFilter, limit);
        if (CollectionUtils.isEmpty(memberExtDOList)) {
            return Collections.emptyList();
        }

        return memberExtDOList.stream()
                .map(MemberConvert::toMemberVO).collect(Collectors.toList());
    }

    @Override
    public MemberVO getByUuid(String uuid) {
        MemberDO memberDO = mapperMemberService.getByUuid(uuid);
        if (memberDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", uuid, ">");
        }
        MemberVO memberVO = MemberConvert.toMemberVO(memberDO);

        OccupationDO occupationDO = mapperOccupationService.getByUuid(memberDO.getOccupationUuid());
        memberVO.setOccupationName(occupationDO.getName());
        return memberVO;
    }


}
