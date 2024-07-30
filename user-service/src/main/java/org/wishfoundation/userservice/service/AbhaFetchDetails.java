package org.wishfoundation.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.ABHAUserDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.CipherType;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.FieldType;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.EncryptKeyRequest;
import org.wishfoundation.userservice.request.abha.*;
import org.wishfoundation.userservice.response.abha.AbhaOTPVerificationResponse;
import org.wishfoundation.userservice.response.abha.GeneralAbhaResponse;
import org.wishfoundation.userservice.response.abha.GetABHAResponse;
import org.wishfoundation.userservice.security.JWTService;
import org.wishfoundation.userservice.utils.AbhaApiUtils;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.HealthUtils;
import org.wishfoundation.userservice.utils.Helper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@Service
public class AbhaFetchDetails {

    @Autowired
    private EnvironmentConfig env;

    @Autowired
    private WebClient.Builder webClient;

    @Autowired
    private AbhaApiUtils abhaApiUtils;
    
    @Autowired
    JWTService jwtService;
    @Autowired
    private ABHAUserDetailsRepository abhaRepo;
    @Autowired
    private YatriPulseUsersRepository usersRepo;

    @Autowired
    private HealthUtils healthUtils;


    public String generateGatewayToken() {
        WebClient client = webClient.baseUrl(env.getGatewayHostname()).build();
        Map<String, String> m = new HashMap<>();
        m.put("clientId", env.getClientAccessKey());
        m.put("clientSecret", env.getClientSecretKey());
        m.put("grantType", env.getGrantType());

        URI uri = UriComponentsBuilder.fromUriString(env.getGatewayHostname())
                .path("/gateway/v0.5/sessions")
                .build(true)
                .toUri();
        Map<String, Object> myData = client.post().uri(uri).bodyValue(m).retrieve().bodyToMono(Map.class).onErrorMap(ex -> new RuntimeException("Failed with an error", ex))
                .block();

        String token = String.valueOf(myData.get("accessToken"));
        return token;
    }

    // THIS METHOD ONLY USED INTERNALLY.
    // TODO exception handling.
    public AbhaOTPVerificationResponse getAbhaAuthDetails(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }
        if (ObjectUtils.isEmpty(request.getMobile())) {
            throw new WishFoundationException(ErrorCode.INVALID_PHONE_NUMBER.getCode(), ErrorCode.INVALID_PHONE_NUMBER.getMessage());
        }

        abhaApiUtils.checkValidation(request.getOtp() , FieldType.OTP);
        abhaApiUtils.checkValidation(request.getMobile(), FieldType.MOBILE);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String otpEncryptValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());

        AbhaOTPVerificationRequest abhaOTPVerificationRequest = AbhaOTPVerificationRequest.builder()
                .authData(AuthData.builder()
                        .authMethods(List.of("otp"))
                        .otp(Otp.builder()
                                .timeStamp(Helper.getSimpleTimeStamp())
                                .txnId(request.getTxnId())
                                .otpValue(otpEncryptValue)
                                .mobile(request.getMobile())
                                .build())
                        .build())
                .consent(Consent.builder()
                        .code("abha-enrollment")
                        .version("1.4")
                        .build())
                .build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/enrollment/enrol/byAadhaar")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPVerificationRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();

        return abhaOTPVerificationResponse;
    }



    public GeneralAbhaResponse fetchAbhaPdfV2(AbhaRegistrationRequest request) {
        request.setHost(env.getAbhaV2Hostname());
        request.setApiRouteSuffix("/api/v2/account/getCard");
        request.setExtenstion("_pdf.pdf");
        return fetchAbhaDetail(request);
    }
    public GeneralAbhaResponse fetchAbhaCardV2(AbhaRegistrationRequest request) {
        request.setHost(env.getAbhaV2Hostname());
        request.setApiRouteSuffix("/api/v2/account/getPngCard");
        request.setExtenstion("_card.png");
        return fetchAbhaDetail(request);
    }
    public GeneralAbhaResponse fetchAbhaQRV2(AbhaRegistrationRequest request) {
        request.setHost(env.getAbhaV2Hostname());
        request.setApiRouteSuffix("/api/v2/account/qrCode");
        request.setExtenstion("_qr.png");
        return fetchAbhaDetail(request);
    }


    public GeneralAbhaResponse fetchAbhaDetail(AbhaRegistrationRequest request) {

        GeneralAbhaResponse response = new GeneralAbhaResponse();
        try {
            String extenstion = request.getExtenstion();
            UUID userId = UserContext.getUserId();

            System.out.println("userId : "+userId.toString());

            String key = EnvironmentConfig.S3_PROFILE_DETAILS + userId + "/" + UserContext.getCurrentUserName() + extenstion;
            String finalReportPath = "user-service" + File.separator + "uploads" + File.separator + key ;
            if (!StringUtils.hasLength(request.getAbhaToken()) && !StringUtils.hasLength(request.getOtp())) {
                try {
                    if (StringUtils.hasLength(request.getAadharNumber())) {
                        Optional<YatriPulseUsers> byId = usersRepo.findById(userId);
                        if (byId.isPresent()) {
                            YatriPulseUsers yatriPulseUsers = byId.get();
                            UUID abhaUserId = yatriPulseUsers.getAbhaUserId();

                            Optional<ABHAUserDetails> abhaById = abhaRepo.findById(abhaUserId);
                            if (abhaById.isPresent()) {
                                ABHAUserDetails abhaUserDetails = abhaById.get();
                                String dateOfBirth = abhaUserDetails.getDateOfBirth().replaceAll("/", "-");
                                request.setDateOfBirth(dateOfBirth);
                                request.setGender(String.valueOf(abhaUserDetails.getGender().charAt(0)).toUpperCase());
                                request.setStateCode(abhaUserDetails.getStateCode());
                                request.setDistrictCode(abhaUserDetails.getDistrictCode());
                                request.setName(abhaUserDetails.getFullName());
                                request.setMobileNumber(abhaUserDetails.getPhoneNumber());

                                GetABHAResponse abhaProfile = getAbhaProfile(request);
                                request.setAbhaToken(abhaProfile.getJwtResponse().getToken());
                            }
                        }
                    } else {
                        String objectUrl = Helper.getFileContent(Helper.currentDirectory(),finalReportPath);
                        response.setFileBase64(objectUrl);
                        return response;
                    }
                } catch (Exception e) {
                    throw new WishFoundationException("Need to re-verify user");
                }
            }
            System.out.println("finalReportPath : "+finalReportPath);
            System.out.println("key : "+key);

            String token = "";
            if (StringUtils.hasLength(request.getAbhaToken()))
                token = request.getAbhaToken();
            else {
                AbhaOTPVerificationResponse abhaAuthDetails = getAbhaAuthDetails(request);
                token = abhaAuthDetails.getTokens().getToken();
            }


            WebClient client = webClient.baseUrl(request.getHost()).build();
            byte[] bytes = client.get()
                    .uri(request.getApiRouteSuffix())
                    .header("Authorization", "Bearer " + generateGatewayToken())
                    .header("X-token", "Bearer " + token)
                    .header("TIMESTAMP", Helper.getIsoTimeStamp())
                    .header("REQUEST-ID", UUID.randomUUID().toString())
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            Helper.fileCreate(Helper.currentDirectory(),finalReportPath,bytes);
            return response;
        } catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }
    }


    public String getObjectUrl(S3Client s3Client, AbhaRegistrationRequest request, String key) {
        try (S3Presigner s3Presigner = S3Presigner.builder().region(Region.AP_SOUTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(env.getAccessKey(), env.getSecretKey())))
                .build()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(Helper.getConfigBucket(Region.AP_SOUTH_1.toString())).key(key).build();
            s3Client.getObject(objectRequest);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(1))
                    .getObjectRequest(objectRequest)
                    .build();
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            String externalForm = presignedRequest.url().toExternalForm();
            return externalForm;
        }  catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }
    }



    //-----------------------------//

    public GeneralAbhaResponse fetchAbhaCard(AbhaRegistrationRequest request) {
        request.setHost(env.abhaRegistrationHost);
        request.setApiRouteSuffix("/v3/profile/account/abha-card");
        request.setExtenstion("_card.svg");
        try {
            return fetchAbhaDetail(request);
        }catch (Exception e){
            if(!request.isBinded()){
                request.setBinded(true);
                request.setAbhaToken(null);
                request.setOtp(null);
                return fetchAbhaCardV2(request);
            }
            throw new WishFoundationException(e.getMessage());
        }

    }

    public GeneralAbhaResponse fetchAbhaQR(AbhaRegistrationRequest request) {
        request.setHost(env.abhaRegistrationHost);
        request.setApiRouteSuffix("/v3/profile/account/qrCode");
        request.setExtenstion("_qr.png");
        return fetchAbhaDetail(request);
    }

    public GeneralAbhaResponse fetchAbhaPdf(AbhaRegistrationRequest request) {
        request.setHost(env.getAbhaV2Hostname());
        request.setApiRouteSuffix("/api/v2/account/getCard");
        request.setExtenstion("_pdf.pdf");
        try {
           return fetchAbhaDetail(request);
        }catch (Exception e){
            if(!request.isBinded()){
                request.setBinded(true);
                request.setAbhaToken(null);
                request.setOtp(null);
                return fetchAbhaDetail(request);
            }
            throw new WishFoundationException(e.getMessage());
        }
    }



    private GetABHAResponse getAbhaProfile(AbhaRegistrationRequest request){
        try {
            System.out.println("request : "+Helper.MAPPER.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        abhaApiUtils.checkValidation(request.getAadharNumber(), FieldType.AADHAR_CARD);

        WebClient client = webClient.baseUrl(env.getAbhaV2Hostname()).build();
        GetABHAResponse profile = client.post()
                .uri("/api/v2/hid/benefit/createHealthId/demo/auth")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        try {
            System.out.println("profile : "+Helper.MAPPER.writeValueAsString(profile));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return profile;
    }



}
