package com.lylh.biz.service.impl;

import com.lylh.biz.convert.GroupConvert;
import com.lylh.biz.convert.MemberConvert;
import com.lylh.biz.convert.TeamConvert;
import com.lylh.biz.model.dto.TeamDTO;
import com.lylh.biz.model.vo.GroupWithMemberVO;
import com.lylh.biz.model.vo.MemberVO;
import com.lylh.biz.model.vo.TeamVO;
import com.lylh.biz.model.vo.TeamWithGroupVO;
import com.lylh.biz.service.TeamService;
import com.lylh.common.model.BizException;
import com.lylh.common.model.ResponseCode;
import com.lylh.repository.biz.MapperGroupService;
import com.lylh.repository.biz.MapperMemberService;
import com.lylh.repository.biz.MapperTeamService;
import com.lylh.repository.entity.GroupDO;
import com.lylh.repository.entity.MemberDO;
import com.lylh.repository.entity.TeamDO;
import com.lylh.repository.entity.ext.GroupExtDO;
import com.lylh.repository.entity.ext.MemberExtDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private MapperTeamService mapperTeamService;

    @Autowired
    private MapperGroupService mapperGroupService;

    @Autowired
    private MapperMemberService mapperMemberService;

    @Override
    public String create(TeamDTO teamDTO) {
        TeamDO teamDO = TeamConvert.toTeamDO(teamDTO);
        mapperTeamService.save(teamDO);
        if (! CollectionUtils.isEmpty(teamDTO.getGroupList())) {
            mapperGroupService.saveBatch(teamDTO.getGroupList().stream()
                    .map(item -> GroupConvert.toGroupDO(item, teamDO.getUuid())).collect(Collectors.toList()));

        }
        return teamDO.getUuid();
    }

    @Override
    public void createBatch(List<TeamDTO> teamDTOList) {
        teamDTOList.forEach(this::create);
    }

    @Override
    public void update(TeamDTO teamDTO) {
        TeamDO teamDO = mapperTeamService.getByUuid(teamDTO.getUuid());
        if (teamDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", teamDTO.getUuid(), ">");
        }
        mapperTeamService.updateTeam(teamDO, teamDTO.getSerialNumber(), teamDTO.getDescription());
    }

    @Override
    public void updateBatch(List<TeamDTO> teamDTOList) {
        teamDTOList.forEach(this::update);
    }

    @Override
    public void delete(String uuid) {
        mapperTeamService.deleteByUuid(uuid);
        mapperGroupService.deleteByTeamUuid(uuid);
    }

    @Override
    public TeamVO getByUuid(String uuid) {
        TeamDO teamDO = mapperTeamService.getByUuid(uuid);
        if (teamDO == null) {
            throw new BizException(ResponseCode.NO_DATA, "uuid:<", uuid, ">");
        }
        return TeamConvert.toTeamVO(teamDO);
    }

    @Override
    public TeamWithGroupVO getTeamWithGroupByUuid(String uuid) {
        //1、取team
        TeamWithGroupVO teamWithGroupVO = (TeamWithGroupVO) getByUuid(uuid);
        //2、取team下的groups，并携带assignment信息
        List<GroupExtDO> groupExtDOList = mapperGroupService.getGroupWithAssignmentByTeamUuid(teamWithGroupVO.getUuid());
        if (! CollectionUtils.isEmpty(groupExtDOList)) {
            List<GroupWithMemberVO> groupWithMemberVOList = groupExtDOList.stream()
                    .map(GroupConvert::toGroupWithMemberVO).collect(Collectors.toList());

            //3、取groups下的所有members
            List<String> groupUuids = groupExtDOList.stream()
                    .map(GroupDO::getUuid).collect(Collectors.toList());
            List<MemberExtDO> memberExtDOList = mapperMemberService.getMemberWithGroupListByGroupUuids(groupUuids);

            //4、将携带assignment信息的memberList，塞进对应group -> 做map减少循环
            if (! CollectionUtils.isEmpty(memberExtDOList)) {
                Map<String, List<MemberExtDO>> memberGroupMap = memberExtDOList.stream()
                        .collect(Collectors.groupingBy(MemberExtDO::getGroupUuid));
                groupWithMemberVOList.forEach(groupWithMemberVO -> {
                    List<MemberExtDO> memberExtDOsForGroup = memberGroupMap.get(groupWithMemberVO.getUuid());

                    if (!CollectionUtils.isEmpty(memberExtDOsForGroup)) {
                        groupWithMemberVO.setMemberList(memberExtDOsForGroup.stream()
                                .map(MemberConvert::toMemberVO).collect(Collectors.toList()));
                    }
                });
            }
            //5、将携带member信息的groups，塞进team
            teamWithGroupVO.setGroupList(groupWithMemberVOList);
        }
        return teamWithGroupVO;
    }

    @Override
    public List<TeamVO> listTeam(Integer limit) {
        List<TeamDO> teamDOList = mapperTeamService.listTeam(limit);
        if (CollectionUtils.isEmpty(teamDOList)) {
            return Collections.emptyList();
        }
        return teamDOList.stream()
                .map(TeamConvert::toTeamVO).collect(Collectors.toList());
    }
}
