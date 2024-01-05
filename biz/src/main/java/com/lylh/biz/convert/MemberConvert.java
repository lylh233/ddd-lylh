package com.lylh.biz.convert;

import com.lylh.biz.model.dto.MemberDTO;
import com.lylh.biz.model.vo.MemberVO;
import com.lylh.common.utils.UUIDUtils;
import com.lylh.repository.entity.MemberDO;
import com.lylh.repository.entity.ext.MemberExtDO;

public class MemberConvert {

    public static MemberDO toMemberDO(MemberDTO memberDTO) {
        MemberDO memberDO = new MemberDO();
        memberDO.setUuid(UUIDUtils.simpleUUID());
        memberDO.setName(memberDTO.getName());
        memberDO.setPosition(memberDTO.getPosition());
        return memberDO;
    }

    public static MemberVO toMemberVO(MemberDO memberDO) {
        MemberVO memberVO = new MemberVO();
        memberVO.setUuid(memberDO.getUuid());
        memberVO.setName(memberDO.getName());
        memberVO.setPosition(memberDO.getPosition());
        memberVO.setOccupationUuid(memberDO.getOccupationUuid());

        return memberVO;
    }

    public static MemberVO toMemberVO(MemberExtDO memberExtDO) {
        MemberVO memberVO = new MemberVO();
        memberVO.setUuid(memberExtDO.getUuid());
        memberVO.setName(memberExtDO.getName());
        memberVO.setPosition(memberExtDO.getPosition());
        memberVO.setOccupationUuid(memberExtDO.getOccupationUuid());

        memberVO.setOccupationName(memberExtDO.getOccupationName());
        return memberVO;
    }
}
