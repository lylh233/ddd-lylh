package com.lylh.common.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "lylh.auth")
public class AuthConfigProperties {

    private boolean verifyToken;

}
