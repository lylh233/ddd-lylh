package com.lylh.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    @Test
    void should_returnNull_when_millisecondMissing() {
        assertThat(DateUtils.changeMillisecondToTimeStr(null)).isNull();
    }

    @Test
    void should_padEachUnit_when_formatDuration() {
        assertThat(DateUtils.changeMillisecondToTimeStr(0L)).isEqualTo("00:00:00");
        assertThat(DateUtils.changeMillisecondToTimeStr(3661000L)).isEqualTo("01:01:01");
    }
}
