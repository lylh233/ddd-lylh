package com.lylh.biz.convert;

import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.enums.UserTypeEnum;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.common.model.Constants;
import com.lylh.common.model.TokenPayload;
import com.lylh.common.utils.SecureUtils;
import com.lylh.common.utils.UUIDUtils;
import com.lylh.repository.entity.UserDO;


public class UserConvert {

    public static UserDO toUserDO(UserDTO userDTO) {
        UserDO userDO = new UserDO();
        userDO.setUuid(UUIDUtils.simpleUUID());
        userDO.setUsername(userDTO.getUsername());
        userDO.setUserType(userDTO.getUserType());
        userDO.setName(userDTO.getName());

        userDO.setPassword(SecureUtils.encryptWithAES(userDTO.getPassword(), Constants.SECRET_PASSWORD));
        return userDO;
    }

    public static UserVO toUserVO(TokenPayload tokenPayload) {
        UserVO userVO = new UserVO();
        userVO.setUuid(tokenPayload.getUuid());
        userVO.setUsername(tokenPayload.getUsername());
        userVO.setUserType(tokenPayload.getUserType());
        userVO.setName(tokenPayload.getName());

        userVO.setUserTypeDesc(UserTypeEnum.parse(tokenPayload.getUserType()).getDesc());
        return userVO;
    }

    public static UserVO toUserVO(UserDO userDO) {
        UserVO userVO = new UserVO();
        userVO.setUuid(userDO.getUuid());
        userVO.setUsername(userDO.getUsername());
        userVO.setName(userDO.getName());
        userVO.setUserType(userDO.getUserType());

        userVO.setUserTypeDesc(UserTypeEnum.parse(userDO.getUserType()).getDesc());
        return userVO;
    }

    public static TokenPayload toTokenPayload(UserDO userDO) {
        TokenPayload tokenPayload = new TokenPayload();
        tokenPayload.setUuid(userDO.getUuid());
        tokenPayload.setName(userDO.getName());
        tokenPayload.setUsername(userDO.getUsername());
        tokenPayload.setUserType(userDO.getUserType());

        return tokenPayload;
    }
}
