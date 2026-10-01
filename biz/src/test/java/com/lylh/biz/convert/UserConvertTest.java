package com.lylh.biz.convert;

import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.enums.UserTypeEnum;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.common.model.Constants;
import com.lylh.common.model.TokenPayload;
import com.lylh.common.utils.SecureUtils;
import com.lylh.repository.entity.UserDO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserConvertTest {

    @Test
    void should_encryptPassword_when_convertToUserDO() {
        UserDTO dto = new UserDTO();
        dto.setUsername("admin");
        dto.setName("浩");
        dto.setPassword("123456");
        dto.setUserType(UserTypeEnum.PUBLIC_USER.getType());

        UserDO user = UserConvert.toUserDO(dto);

        assertThat(user.getUuid()).isNotBlank();
        assertThat(user.getPassword()).isEqualTo(SecureUtils.encryptWithAES("123456", Constants.SECRET_PASSWORD));
        assertThat(user.getPassword()).isNotEqualTo("123456");
    }

    @Test
    void should_fillTypeDesc_when_convertTokenPayload() {
        TokenPayload payload = new TokenPayload();
        payload.setUuid("u-1");
        payload.setUsername("admin");
        payload.setName("浩");
        payload.setUserType(UserTypeEnum.STAFF.getType());

        UserVO user = UserConvert.toUserVO(payload);

        assertThat(user.getUserTypeDesc()).isEqualTo("工作人员");
        assertThat(user.getUuid()).isEqualTo("u-1");
    }

    @Test
    void should_keepIdentity_when_convertToTokenPayload() {
        UserDO user = new UserDO();
        user.setUuid("u-1");
        user.setUsername("admin");
        user.setName("浩");
        user.setUserType(UserTypeEnum.STAFF.getType());
        user.setPassword("cipher");

        TokenPayload payload = UserConvert.toTokenPayload(user);

        assertThat(payload.getUuid()).isEqualTo("u-1");
        assertThat(payload.getUsername()).isEqualTo("admin");
        assertThat(payload.toString()).doesNotContain("cipher");
    }
}
