package org.wishfoundation.iomtdeviceinventory.utils;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Data
public class EnvironmentConfig {
    public static final String YATRI_SERVICE_HOST = System.getenv("YATRI_SERVICE_HOST")!= null ? System.getenv("YATRI_SERVICE_HOST"): "https://api.userservice.yatripulse.in/";

    public static final List<String> IGNORED_API_FOR_AUTH = new ArrayList<>(Arrays.asList("/", "/api/iomt/*"));
}
