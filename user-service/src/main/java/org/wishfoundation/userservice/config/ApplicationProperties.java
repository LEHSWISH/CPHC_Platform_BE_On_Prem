package org.wishfoundation.userservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.wishfoundation.userservice.request.BasicAuthConfiguration;

import java.util.ArrayList;
import java.util.List;

@EnableConfigurationProperties
@Configuration
@Getter
@Setter
@ConfigurationProperties("")
public class ApplicationProperties {

    private GlobalDataSource database;
    private BasicAuthConfiguration basicAuth;
    private List<String> activeProfiles = new ArrayList<>();

    @Autowired
    private Environment environment;

    @PostConstruct
    private void set() {

        String[] active = environment.getActiveProfiles();
        for (int i = 0; i < active.length; i++) {
            activeProfiles.add(active[i]);
        }
    }

    public boolean isProduction() {
        return activeProfiles != null && activeProfiles.contains("prod");
    }

    public boolean isStaging() {
        return activeProfiles != null && activeProfiles.contains("stage");
    }

    public boolean isDeployment() {
        return activeProfiles != null && activeProfiles.contains("deployment");
    }

    public boolean isDev() {
        return activeProfiles != null && activeProfiles.contains("dev");
    }
}
