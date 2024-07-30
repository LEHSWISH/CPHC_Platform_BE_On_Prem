package org.wishfoundation.abhaservice.aws.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wishfoundation.abhaservice.config.EnvironmentConfig;
import org.wishfoundation.abhaservice.utils.Helper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@RequiredArgsConstructor
@Service
public class S3Wrapper {

    private final EnvironmentConfig environmentConfig;

    public byte[] getObjectAsBytes(String key) {
        try (S3Client s3Client = S3Client.builder().region(Region.AP_SOUTH_1).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(environmentConfig.getAccessKey(), environmentConfig.getSecretKey())))
                .build()) {
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(key)
                    .bucket(Helper.getConfigBucket(Region.AP_SOUTH_1.toString()))
                    .build();
            try {
                ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
                return objectBytes.asByteArray();
            } catch (NoSuchKeyException e) {
                return new byte[0];
            }
        }
    }

    public void putObject(String key, File file) {
        try (S3Client s3Client = S3Client.builder().region(Region.AP_SOUTH_1).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(environmentConfig.getAccessKey(), environmentConfig.getSecretKey())))
                .build()) {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(Helper.getConfigBucket(Region.AP_SOUTH_1.toString()))
                    .key(key)
                    .build();
            s3Client.putObject(objectRequest, RequestBody.fromFile(file));
        }
    }

}
