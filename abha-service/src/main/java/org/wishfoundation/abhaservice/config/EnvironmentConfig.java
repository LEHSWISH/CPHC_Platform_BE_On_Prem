package org.wishfoundation.abhaservice.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;

@Data
@Configuration
public class EnvironmentConfig {

    public static final String CONFIG_BUCKET_PATH  = System.getenv("CONFIG_BUCKET_PATH")!= null ? System.getenv("CONFIG_BUCKET_PATH"): "wishfoundation.config.dev.ap-south-1";

    public static final EnvironmentVariableCredentialsProvider awsCredentialsProvider = EnvironmentVariableCredentialsProvider.create();

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${baseUrl.userService}")
    private String userService;

    @Value("${baseUrl.healthService}")
    private String healthService;
}
