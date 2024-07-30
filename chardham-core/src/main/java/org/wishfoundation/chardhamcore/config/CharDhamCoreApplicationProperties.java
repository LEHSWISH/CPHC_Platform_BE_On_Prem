package org.wishfoundation.chardhamcore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.wishfoundation.chardhamcore.request.BasicAuthConfiguration;

@EnableConfigurationProperties
@Configuration
@Getter
@Setter
@ConfigurationProperties("")
public class CharDhamCoreApplicationProperties {

    private GlobalDataSource database;
    private GlobalDataSource userDatabase;
    private BasicAuthConfiguration basicAuth;

}
