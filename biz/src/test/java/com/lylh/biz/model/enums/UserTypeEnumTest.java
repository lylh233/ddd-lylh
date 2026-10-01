package com.lylh.biz.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTypeEnumTest {

    @Test
    void should_returnNullType_when_valueUnknown() {
        assertThat(UserTypeEnum.parse(99)).isEqualTo(UserTypeEnum.NULL);
    }

    @Test
    void should_returnStaff_when_valueMatches() {
        assertThat(UserTypeEnum.parse(UserTypeEnum.STAFF.getType()).getDesc()).isEqualTo("工作人员");
    }
}
