package org.wishfoundation.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.ABHAUserDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.AbhaScope;
import org.wishfoundation.userservice.enums.CipherType;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.FieldType;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.EncryptKeyRequest;
import org.wishfoundation.userservice.request.abha.AbhaOTPEnrolRequest;
import org.wishfoundation.userservice.request.abha.AbhaRegistrationRequest;
import org.wishfoundation.userservice.request.abha.AuthData;
import org.wishfoundation.userservice.request.abha.Otp;
import org.wishfoundation.userservice.response.abha.*;
import org.wishfoundation.userservice.utils.AbhaApiUtils;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.Helper;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

@RequiredArgsConstructor
@Service
public class AbhaVerificationServiceImpl {

//    private static final String GATEWAY_SESSION_TOKEN_HOST = "https://live.abdm.gov.in";
    //    private static final String ABHA_VERIFICATION_API_HOST = "https://healthidsbx.abdm.gov.in";

    @Autowired
    private EnvironmentConfig env;
    private final WebClient.Builder webClient;
    @Autowired
    public PasswordEncoder passwordEncoder;
    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepo;
    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;
    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;
    @Autowired
    private AbhaFetchDetails abaAbhaFetchDetails;
    @Autowired
    private AbhaApiUtils abhaApiUtils;


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
        Map<String, Object> myData = client.post().uri(uri).bodyValue(m).retrieve().bodyToMono(Map.class)
                .block();

        String token = String.valueOf(myData.get("accessToken"));
        return token;
    }

    public GeneralAbhaResponse mapABHAResponseToGeneralResponse(AbhaOTPVerificationResponse responseData, AbhaUserDetailsResponse abhaUserDetails) {
        String abhaNumber = "";
        String mobile = "";
        String txnId = Optional.ofNullable(responseData.getTxnId()).orElse("");
        String message = Optional.ofNullable(responseData.getMessage()).orElse("");


        if (!ObjectUtils.isEmpty(abhaUserDetails)) {
            abhaNumber = Optional.ofNullable(abhaUserDetails.getAbhaNumber()).orElse("");
            mobile = Optional.ofNullable(abhaUserDetails.getMobile()).orElse("");
        }
        return GeneralAbhaResponse.builder()
                .txnId(txnId)
                .message(message)
                .abhaNumber(abhaNumber)
//                .tokens(responseData.getTokens())
                .mobile(mobile)
                .authType("v3")
                .build();
    }

    public GeneralAbhaResponse mapABHAResponseToGeneralResponse(GetABHAResponse responseData, AbhaRegistrationRequest request) {
        String abhaNumber = "";
        String mobile = "";
        String txnId = Optional.ofNullable(request.getTxnId()).orElse("");
        String message = Optional.ofNullable(responseData.getMessage()).orElse("");


        if (!ObjectUtils.isEmpty(responseData)) {
            abhaNumber = Optional.ofNullable(StringUtils.hasLength(responseData.getHealthIdNumber()) ? responseData.getHealthIdNumber() : responseData.getABHANumber()).orElse("");
            mobile = Optional.ofNullable(responseData.getMobile()).orElse("");
        }
        return GeneralAbhaResponse.builder()
                .txnId(txnId)
                .message(message)
                .abhaNumber(abhaNumber)
                .mobile(mobile)
                .firstName(responseData.getFirstName())
//                .tokens(responseData.getJwtResponse())
                .authType("v3")
                .build();
    }


    public ResponseEntity<ABHAUserDetails> saveAbhaDetails(GetABHAResponse abhaProfileRequest) {

        YatriPulseUsers yatriPulseUsers = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(UserContext.getCurrentUserName(), UserContext.getCurrentPhoneNumber());
        if (ObjectUtils.isEmpty(yatriPulseUsers))
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage());
        if (!ObjectUtils.isEmpty(yatriPulseUsers.getAbhaUserId())) {
            Optional<ABHAUserDetails> abhaUserDetailsOpt = abhaUserDetailsRepo.findById(yatriPulseUsers.getAbhaUserId());
            if (abhaUserDetailsOpt.isPresent())
                return new ResponseEntity<>(abhaUserDetailsOpt.get(), HttpStatus.CREATED);
        }

        String abhaNumberId = StringUtils.hasLength(abhaProfileRequest.getHealthIdNumber()) ? abhaProfileRequest.getHealthIdNumber() : abhaProfileRequest.getABHANumber();
        String encrypt = Helper.encrypt(abhaNumberId);
        if (StringUtils.hasLength(encrypt)) {
            Optional<ABHAUserDetails> byAbhaNumber = abhaUserDetailsRepo.findByAbhaNumber(encrypt);
            if (byAbhaNumber.isPresent())
                throw new WishFoundationException(ErrorCode.ABHA_LINKED_WITH_ANOTHER_REGISTERED_USER.getCode(),ErrorCode.ABHA_LINKED_WITH_ANOTHER_REGISTERED_USER.getMessage());
        }

        ABHAUserDetails abhaUserDetails = new ABHAUserDetails();


        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaNumber, encrypt);
        Helper.updateFieldIfNotNull(abhaUserDetails::setFirstName, abhaProfileRequest.getFirstName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setMiddleName, abhaProfileRequest.getMiddleName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setLastName, abhaProfileRequest.getLastName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setDateOfBirth, setDateOfBirth(abhaProfileRequest.getYearOfBirth(),
                abhaProfileRequest.getMonthOfBirth(), abhaProfileRequest.getDayOfBirth()));
        Helper.updateFieldIfNotNull(abhaUserDetails::setGender, Helper.setGender(abhaProfileRequest.getGender()));

//TODO : need to save profile photo in s3 and save path to pathfolder

        Helper.updateFieldIfNotNull(abhaUserDetails::setPhoneNumber, abhaProfileRequest.getMobile());
        Helper.updateFieldIfNotNull(abhaUserDetails::setEmailId, abhaProfileRequest.getEmail());
        Helper.updateFieldIfNotNull(abhaUserDetails::setPhrAddress, Helper.encryptList(abhaProfileRequest.getPhrAddress()));


        Helper.updateFieldIfNotNull(abhaUserDetails::setAddress, abhaProfileRequest.getAddress());
        Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictName, abhaProfileRequest.getDistrictName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictCode, abhaProfileRequest.getDistrictCode());
        Helper.updateFieldIfNotNull(abhaUserDetails::setStateName, abhaProfileRequest.getStateName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setStateCode, abhaProfileRequest.getStateCode());
        Helper.updateFieldIfNotNull(abhaUserDetails::setPinCode, abhaProfileRequest.getPincode());
        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaType, abhaProfileRequest.getAbhaType());
        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaStatus, abhaProfileRequest.getStatus());
        Helper.updateFieldIfNotNull(abhaUserDetails::setFullName, abhaProfileRequest.getName());
        abhaUserDetails.setYatriPulseUserId(UserContext.getUserId());

        yatriPulseUsers.setAbhaUserId(abhaUserDetailsRepo.save(abhaUserDetails).getId());
        yatriPulseUsersRepo.save(yatriPulseUsers);
        return new ResponseEntity<>(abhaUserDetails, HttpStatus.CREATED);
    }


    public String setDateOfBirth(String year, String month, String day) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            String dateString = String.format("%s/%s/%s", day, month, year);
            Date date = dateFormat.parse(dateString);
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();  // Handle parsing/formatting exceptions as needed
        }
        return "";
    }

    public GeneralAbhaResponse abhaAddressMobileLoginOtp(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getHealthid())) {
            throw new WishFoundationException(ErrorCode.INVALID_HEALTH_ID.getCode(), ErrorCode.INVALID_HEALTH_ID.getMessage());
        }
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);

        WebClient client = webClient.baseUrl(env.getAbhaV2Hostname()).build();
        request.setAuthMethod("MOBILE_OTP");

        GeneralAbhaResponse response = client.post()
                .uri("/api/v2/auth/init")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeneralAbhaResponse.class)
                .block();

        return response;
    }

    public GeneralAbhaResponse abhaAddressMobileOtpVerify(AbhaRegistrationRequest request) {
        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }
        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);

        WebClient client = webClient.baseUrl(env.getAbhaV2Hostname()).build();
        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V2_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_PKCS1Padding.getCipherType()).build());

        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .otp(encryptedValue)
                .txnId(request.getTxnId()).build();

        Tokens token = client.post()
                .uri("/api/v2/auth/confirmWithMobileOTP")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(Tokens.class)
                .block();


        String xToken = token.getToken();
        if(!StringUtils.hasLength(xToken)){
            throw new WishFoundationException(ErrorCode.INVALID_MOBILE_OTP.getCode(),ErrorCode.INVALID_MOBILE_OTP.getMessage(),HttpStatus.BAD_REQUEST);
        }
        GetABHAResponse profile = client.get()
                .uri("/api/v2/account/profile")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-Token", "Bearer " + xToken)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        // SAVING DETAIL IN DB.
        saveAbhaDetails(profile);

        request.setABHANumber(profile.getABHANumber());
        request.setFirstName(profile.getFirstName());
        request.setAbhaToken(xToken);
//        GeneralAbhaResponse pngUrl = abaAbhaFetchDetails.fetchAbhaCardV2(request);
//        abaAbhaFetchDetails.fetchAbhaPdfV2(request);
//        abaAbhaFetchDetails.fetchAbhaQRV2(request);
        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
//        response.setPreSignedUrl(pngUrl.getPreSignedUrl());
        response.setTokens(token);
        response.setAuthType("v2");
        return response;
    }

    public GeneralAbhaResponse mobileLoginOtp(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getMobile())) {
            throw new WishFoundationException(ErrorCode.INVALID_PHONE_NUMBER.getCode(), ErrorCode.INVALID_PHONE_NUMBER.getMessage());
        }
        abhaApiUtils.checkValidation(request.getMobile(), FieldType.MOBILE);
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getMobile())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.MOBILE_VERIFY.getScope()))
                .loginHint("mobile")
                .otpSystem("abdm")
                .loginId(encryptedValue).build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/profile/login/request/otp")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();

        return mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse, null);
    }

    public AbhaOTPVerificationResponse mobileLoginVerify(AbhaRegistrationRequest request) {
        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }
        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AuthData authData = AuthData.builder().authMethods(List.of("otp"))
                .otp(Otp.builder().txnId(request.getTxnId()).otpValue(encryptedValue).build())
                .build();

        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.MOBILE_VERIFY.getScope()))
                .authData(authData)
                .build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/profile/login/verify")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();
        return abhaOTPVerificationResponse;
    }

    public GeneralAbhaResponse userVerify(AbhaRegistrationRequest request) {
        AbhaRegistrationRequest req = new AbhaRegistrationRequest();
        req.setABHANumber(request.getABHANumber());
        req.setTxnId(request.getTxnId());
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();
        Tokens tokens = client.post()
                .uri("/v3/profile/login/verify/user")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("T-token", "Bearer " + request.getAbhaToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Tokens.class)
                .block();

        String xToken = tokens.getToken();
        GetABHAResponse profile = client.get()
                .uri("/v3/profile/account")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-token", "Bearer " + xToken)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        // SAVING DETAIL IN DB.
        saveAbhaDetails(profile);

//        request.setABHANumber(abhaNumber);
//        request.setFirstName(profile.getFirstName());
//        request.setXToken(xToken);
//        GeneralAbhaResponse pngUrl = abaAbhaFetchDetails.fetchAbhaCard(request);
//        abaAbhaFetchDetails.fetchAbhaQR(request);
//        abaAbhaFetchDetails.fetchAbhaPdf(request);

        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
//        response.setPreSignedUrl(pngUrl.getPreSignedUrl());
        response.setTokens(tokens);
        return response;
    }

    public GeneralAbhaResponse abhaAddressLoginOtp(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getHealthid())) {
            throw new WishFoundationException(ErrorCode.INVALID_HEALTH_ID.getCode(), ErrorCode.INVALID_HEALTH_ID.getMessage());
        }
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);
        WebClient client = webClient.baseUrl(env.getAbhaV2Hostname()).build();
        request.setAuthMethod("AADHAAR_OTP");

        GeneralAbhaResponse response = client.post()
                .uri("/api/v2/auth/init")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeneralAbhaResponse.class)
                .block();

        return response;
    }

    public GeneralAbhaResponse abhaAddressOtpVerify(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }
        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);

        WebClient client = webClient.baseUrl(env.getAbhaV2Hostname()).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V2_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_PKCS1Padding.getCipherType()).build());

        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .otp(encryptedValue)
                .txnId(request.getTxnId()).build();

        Tokens token = client.post()
                .uri("/api/v2/auth/confirmWithAadhaarOtp")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(Tokens.class)
                .block();

        String xToken = token.getToken();
        if(!StringUtils.hasLength(xToken)){
            throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_OTP.getCode(),ErrorCode.INVALID_AADHAAR_OTP.getMessage(),HttpStatus.BAD_REQUEST);
        }
        GetABHAResponse profile = client.get()
                .uri("/api/v2/account/profile")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-Token", "Bearer " + xToken)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        // SAVING DETAIL IN DB.
        saveAbhaDetails(profile);

//        request.setABHANumber(profile.getABHANumber());
//        request.setFirstName(profile.getFirstName());
//        request.setXToken(xToken);
//        abaAbhaFetchDetails.fetchAbhaPdfV2(request);
//        GeneralAbhaResponse pngUrl = abaAbhaFetchDetails.fetchAbhaCardV2(request);
//        abaAbhaFetchDetails.fetchAbhaQRV2(request);

        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
//        response.setPreSignedUrl(pngUrl.getPreSignedUrl());
        response.setTokens(token);
        response.setAuthType("v2");
        return response;
    }


    public GeneralAbhaResponse abhaIdLoginOtp(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getABHANumber())) {
            throw new WishFoundationException(ErrorCode.INVALID_HEALTH_ID.getCode(), ErrorCode.INVALID_HEALTH_ID.getMessage());
        }
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getABHANumber())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.AADHAAR_VERIFY.getScope()))
                .loginHint("abha-number")
                .otpSystem("aadhaar")
                .loginId(encryptedValue).build();
try{
        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/profile/login/request/otp")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();

        return mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse, null);
    } catch (WishFoundationException e) {
        if (e.getCode().equals("USR49"))
            throw new WishFoundationException(ErrorCode.NO_ABHA_REG_WITH_ABHA_ID.getCode(), ErrorCode.NO_ABHA_REG_WITH_ABHA_ID.getMessage());
        else throw e;
    }
    }

    public GeneralAbhaResponse abhaIdLoginVerify(AbhaRegistrationRequest request) {
        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }
        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AuthData authData = AuthData.builder().authMethods(List.of("otp"))
                .otp(Otp.builder().txnId(request.getTxnId()).otpValue(encryptedValue).build())
                .build();

        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.AADHAAR_VERIFY.getScope()))
                .authData(authData)
                .build();
        AbhaOTPVerificationResponse abhaOTPVerificationResponse  = client.post()
                        .uri("/v3/profile/login/verify")
                        .header("Authorization", "Bearer " + generateGatewayToken())
                        .header("TIMESTAMP", Helper.getIsoTimeStamp())
                        .header("REQUEST-ID", UUID.randomUUID().toString())
                        .header("Content-Type", "application/json")
                        .header("Accept", "*/*")
                        .bodyValue(abhaOTPEnrolRequest)
                        .retrieve()
                        .bodyToMono(AbhaOTPVerificationResponse.class)
                        .block();
        String xToken = abhaOTPVerificationResponse.getToken();
        if(!StringUtils.hasLength(xToken)){
            throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_OTP.getCode(),ErrorCode.INVALID_AADHAAR_OTP.getMessage(),HttpStatus.BAD_REQUEST);
        }

        GetABHAResponse profile = client.get()
                .uri("/v3/profile/account")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-token", "Bearer " + xToken)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        // SAVING DETAIL IN DB.
        saveAbhaDetails(profile);

//        request.setABHANumber(profile.getABHANumber());
//        request.setFirstName(profile.getFirstName());
//        request.setXToken(xToken);
//        GeneralAbhaResponse pngUrl = abaAbhaFetchDetails.fetchAbhaCard(request);
//        abaAbhaFetchDetails.fetchAbhaQR(request);
//        abaAbhaFetchDetails.fetchAbhaPdf(request);

        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
//        response.setPreSignedUrl(pngUrl.getPreSignedUrl());
        response.setTokens(Tokens.builder().token(xToken).build());
        return response;
    }

    public GeneralAbhaResponse abhaIdMobileLoginOtp(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getABHANumber())) {
            throw new WishFoundationException(ErrorCode.INVALID_HEALTH_ID.getCode(), ErrorCode.INVALID_HEALTH_ID.getMessage());
        }
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getABHANumber())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.MOBILE_VERIFY.getScope()))
                .loginHint("abha-number")
                .otpSystem("abdm")
                .loginId(encryptedValue).build();

        try {
            AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                    .uri("/v3/profile/login/request/otp")
                    .header("Authorization", "Bearer " + generateGatewayToken())
                    .header("TIMESTAMP", Helper.getIsoTimeStamp())
                    .header("REQUEST-ID", UUID.randomUUID().toString())
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(abhaOTPEnrolRequest)
                    .retrieve()
                    .bodyToMono(AbhaOTPVerificationResponse.class)
                    .block();

            return mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse, null);
        } catch (WishFoundationException e) {
            if (e.getCode().equals("USR49"))
                throw new WishFoundationException(ErrorCode.NO_ABHA_REG_WITH_ABHA_ID.getCode(), ErrorCode.NO_ABHA_REG_WITH_ABHA_ID.getMessage());
            else throw e;
        }
    }

    public GeneralAbhaResponse abhaIdMobileLoginVerify(AbhaRegistrationRequest request) {
        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }
        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AuthData authData = AuthData.builder().authMethods(List.of("otp"))
                .otp(Otp.builder().txnId(request.getTxnId()).otpValue(encryptedValue).build())
                .build();

        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.MOBILE_VERIFY.getScope()))
                .authData(authData)
                .build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/profile/login/verify")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();


        String xToken = abhaOTPVerificationResponse.getToken();
        if(!StringUtils.hasLength(xToken)){
            throw new WishFoundationException(ErrorCode.INVALID_MOBILE_OTP.getCode(),ErrorCode.INVALID_MOBILE_OTP.getMessage(),HttpStatus.BAD_REQUEST);
        }

        GetABHAResponse profile = client.get()
                .uri("/v3/profile/account")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-token", "Bearer " + xToken)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        // SAVING DETAIL IN DB.
        saveAbhaDetails(profile);

//        request.setABHANumber(profile.getABHANumber());
//        request.setFirstName(profile.getFirstName());
//        request.setXToken(xToken);
//        GeneralAbhaResponse pngUrl = abaAbhaFetchDetails.fetchAbhaCard(request);
//        abaAbhaFetchDetails.fetchAbhaQR(request);
//        abaAbhaFetchDetails.fetchAbhaPdf(request);

        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
//        response.setPreSignedUrl(pngUrl.getPreSignedUrl());
        response.setTokens(Tokens.builder().token(xToken).build());
        return response;

    }

    public GeneralAbhaResponse aadhaarLoginOtp(AbhaRegistrationRequest request) {



        if (ObjectUtils.isEmpty(request.getAadhaar())) {
            throw new WishFoundationException("Aadhaar Number "+ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(), ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage());
        }
        request.setAadhaar(Helper.decryptData(request.getAadhaar()));
        abhaApiUtils.checkValidation(request.getAadhaar(),FieldType.AADHAR_CARD);
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getAadhaar())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.AADHAAR_VERIFY.getScope()))
                .loginHint("aadhaar")
                .otpSystem("aadhaar")
                .loginId(encryptedValue).build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = null;
        try{
            abhaOTPVerificationResponse = client.post()

                    .uri("/v3/profile/login/request/otp")
                    .header("Authorization", "Bearer " + generateGatewayToken())
                    .header("TIMESTAMP", Helper.getIsoTimeStamp())
                    .header("REQUEST-ID", UUID.randomUUID().toString())
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .bodyValue(abhaOTPEnrolRequest)
                    .retrieve()
                    .bodyToMono(AbhaOTPVerificationResponse.class)
                    .block();
        }catch(Exception e){
            throw new WishFoundationException(ErrorCode.NO_AADHAAR_REGISTER.getCode(),ErrorCode.NO_AADHAAR_REGISTER.getMessage());
        }

        return mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse, null);
    }

    public GeneralAbhaResponse aadhaarLoginVerify(AbhaRegistrationRequest request) {

        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String encryptedValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AuthData authData = AuthData.builder().authMethods(List.of("otp"))
                .otp(Otp.builder().txnId(request.getTxnId()).otpValue(encryptedValue).build())
                .build();

        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_LOGIN.getScope(), AbhaScope.AADHAAR_VERIFY.getScope()))
                .authData(authData)
                .build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/profile/login/verify")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();

        String xToken = abhaOTPVerificationResponse.getToken();
        if(!StringUtils.hasLength(xToken)){
            throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_OTP.getCode(),ErrorCode.INVALID_AADHAAR_OTP.getMessage(),HttpStatus.BAD_REQUEST);
        }

        GetABHAResponse profile = client.get()
                .uri("/v3/profile/account")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-token", "Bearer " + xToken)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();

        saveAbhaDetails(profile);

        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
        response.setTokens(Tokens.builder().token(xToken).build());
        return response;
    }
}
