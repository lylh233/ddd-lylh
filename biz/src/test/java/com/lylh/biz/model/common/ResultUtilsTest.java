package com.lylh.biz.model.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.common.model.ResponseCode;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ResultUtilsTest {

    @Test
    void should_useSuccessCode_when_wrapData() {
        CommonResult<String> result = ResultUtils.ok("token");

        assertThat(result.getCode()).isEqualTo(ResponseCode.SUCCESS.getCode());
        assertThat(result.getData()).isEqualTo("token");
    }

    @Test
    void should_copyPageInfo_when_wrapPage() {
        Page<String> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList("admin"));
        page.setTotal(1);

        PageListResult<String> result = ResultUtils.ok(page);

        assertThat(result.getData()).containsExactly("admin");
        assertThat(result.getPageInfo().get("items")).isEqualTo(1L);
        assertThat(result.getPageInfo().get("page_num")).isEqualTo(1L);
    }

    @Test
    void should_keepBusinessCode_when_wrapError() {
        CommonResult<?> result = ResultUtils.error(ResponseCode.PASSWORD_ERROR);

        assertThat(result.getCode()).isEqualTo(ResponseCode.PASSWORD_ERROR.getCode());
        assertThat(result.getMsg()).isEqualTo(ResponseCode.PASSWORD_ERROR.getMessage());
    }
}
