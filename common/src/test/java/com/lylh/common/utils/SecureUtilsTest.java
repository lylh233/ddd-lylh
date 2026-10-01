package com.lylh.common.utils;

import com.lylh.common.model.Constants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureUtilsTest {

    @Test
    void should_roundTrip_when_encryptAndDecrypt() {
        String cipher = SecureUtils.encryptWithAES("123456", Constants.SECRET_PASSWORD);

        assertThat(cipher).isNotEqualTo("123456");
        assertThat(SecureUtils.decryptWithAES(cipher, Constants.SECRET_PASSWORD)).isEqualTo("123456");
    }
}
