package org.wishfoundation.healthservice.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;

@Configuration
@Data
public class EnvironmentConfig {

    //TOdo: Pick all these things from environment
//    public static final String YATRI_SERVICE_HOST = "http://yatripulse-dev.centilytics.com/user-service";

    public static final String CONFIG_BUCKET_PATH  = System.getenv("CONFIG_BUCKET_PATH")!= null ? System.getenv("CONFIG_BUCKET_PATH"): "wishfoundation.config.dev.ap-south-1";

    public static final EnvironmentVariableCredentialsProvider awsCredentialsProvider = EnvironmentVariableCredentialsProvider.create();

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${baseUrl.userService}")
    private String userService;

    @Value("${baseUrl.notificationService}")
    private String notificationService;

    @Value("${baseUrl.abhaService}")
    private String abhaService;

}
