package org.wishfoundation.chardhamcore.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Getter
public class EnvironmentConfigCommon {

    @Autowired
    private Environment environment;
    public static final List<String> IGNORED_API_FOR_AUTH = new ArrayList<>(Arrays.asList("/","/api/v1/yatri/validate-user/*", "/api/v1/yatri/login", "/api/v1/yatri/sign-up", "/actuator/**",
            "/api/v1/yatri/send-otp","api/v1/yatri/resend-otp","api/v1/yatri/verify-otp","/api/v1/sms/send-sms","/api/v1/email/send-email", "api/v1/yatri/forget-username","api/v1/yatri/reset-password","/api/v1/yatri/validate/user-name-phone-number","/api/v1/sms/notify-number","/api/v1/state/get-code","/api/v1/district/get-code","/api/v1/email/send-email-smtp"));

    public  static  final String dataSecretKey = "Wish20foundation";

    public String profile = isProduction() ? "prod" : "dev";

    private List<String> activeProfiles = new ArrayList<>();
    @PostConstruct
    private void set() {
        String[] active = environment.getActiveProfiles();
        for (int i = 0; i < active.length; i++) {
            try {
                System.out.println(HelperCommon.MAPPER.writeValueAsString(active[i]));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            activeProfiles.add(active[i]);
        }
    }

    public boolean isDev() {
        return activeProfiles != null && activeProfiles.contains("dev");
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
}


