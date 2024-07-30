package org.wishfoundation.superadmin.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Configuration
@Data
public class EnvironmentConfig {

    public static final String SALT = "$2a$10$CwTycUXWue0Thq9StjUM0u";
    public static final int OTP_LENGTH = 6;
    public static final List<String> IGNORED_API_FOR_AUTH = new ArrayList<>(Arrays.asList("/", "/api/v1/auth/login",  "/api/v1/auth/reset-password", "/api/v1/auth/"));


    @Value("${baseUrl.notificationService}")
    private String notificationService;
}
