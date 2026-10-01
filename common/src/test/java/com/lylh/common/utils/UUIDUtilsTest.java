package com.lylh.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UUIDUtilsTest {

    @Test
    void should_stayStable_when_nameRepeats() {
        assertThat(UUIDUtils.generateByName("admin")).isEqualTo(UUIDUtils.generateByName("admin"));
    }

    @Test
    void should_removeHyphen_when_generateCompactUuid() {
        String uuid = UUIDUtils.generateByNameWithoutHyphen("admin");

        assertThat(uuid).doesNotContain("-").hasSize(32);
    }
}
