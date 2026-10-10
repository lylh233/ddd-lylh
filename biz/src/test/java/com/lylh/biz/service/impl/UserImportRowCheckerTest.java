package com.lylh.biz.service.impl;

import com.lylh.biz.model.enums.UserTypeEnum;
import com.lylh.biz.model.excel.UserImportRow;
import com.lylh.biz.model.excel.UserImportRowChecker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserImportRowCheckerTest {

    @Test
    void should_skip_when_rowBlank() {
        assertThat(UserImportRowChecker.isBlank(new UserImportRow())).isTrue();
        assertThat(UserImportRowChecker.check(new UserImportRow()).isSkipped()).isTrue();
    }

    @Test
    void should_fail_when_usernameMissing() {
        UserImportRow row = validRow();
        row.setUsername(" ");

        UserImportRowChecker.CheckOutcome outcome = UserImportRowChecker.check(row);

        assertThat(outcome.isPassed()).isFalse();
        assertThat(outcome.getReason()).isEqualTo("用户名不能为空");
    }

    @Test
    void should_fail_when_userTypeUnknown() {
        UserImportRow row = validRow();
        row.setUserType("9");

        assertThat(UserImportRowChecker.check(row).getReason()).contains("用户类型");
    }

    @Test
    void should_pass_when_userTypeHasDecimalSuffix() {
        UserImportRow row = validRow();
        row.setUserType("2.0");

        UserImportRowChecker.CheckOutcome outcome = UserImportRowChecker.check(row);

        assertThat(outcome.isPassed()).isTrue();
        assertThat(outcome.getUserType()).isEqualTo(UserTypeEnum.STAFF.getType());
        assertThat(outcome.getUsername()).isEqualTo("admin");
    }

    @Test
    void should_truncateSource_when_cellsTooLong() {
        UserImportRow row = validRow();
        StringBuilder username = new StringBuilder();
        for (int i = 0; i < UserImportRowChecker.MAX_SOURCE_LENGTH; i++) {
            username.append('a');
        }
        row.setUsername(username.toString());

        assertThat(UserImportRowChecker.sourceOf(row)).hasSize(UserImportRowChecker.MAX_SOURCE_LENGTH);
    }

    private UserImportRow validRow() {
        UserImportRow row = new UserImportRow();
        row.setUsername(" admin ");
        row.setName("浩");
        row.setPassword("123456");
        row.setUserType("1");
        return row;
    }
}
