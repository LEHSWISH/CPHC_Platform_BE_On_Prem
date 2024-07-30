package org.wishfoundation.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.wishfoundation.userservice.config.AsyncConfig;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.ABHAUserDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.*;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.EncryptKeyRequest;
import org.wishfoundation.userservice.request.abha.*;
import org.wishfoundation.userservice.request.health.HealthModel;
import org.wishfoundation.userservice.response.abha.ABHAProfile;
import org.wishfoundation.userservice.response.abha.AbhaOTPVerificationResponse;
import org.wishfoundation.userservice.response.abha.GeneralAbhaResponse;
import org.wishfoundation.userservice.response.abha.GetABHAResponse;
import org.wishfoundation.userservice.utils.AbhaApiUtils;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.HealthUtils;
import org.wishfoundation.userservice.utils.Helper;

import java.net.URI;
import java.util.*;

//Todo Error handling of ABDM error Codes

@RequiredArgsConstructor
@Service
public class AbhaRegistrationServiceImpl implements AbhaRegistrationService {

//    public static final String GATEWAY_SESSION_TOKEN_HOST = "https://live.abdm.gov.in";

    private final WebClient.Builder webClient;
    private final EnvironmentConfig env;
    @Autowired
    AbhaFetchDetails abhaFetchDetails;
    @Autowired
    private AsyncConfig asyncConfig;
    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepo;
    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;
    @Autowired
    private AbhaVerificationServiceImpl abhaVerificationServiceImpl;
    @Autowired
    private AbhaApiUtils abhaApiUtils;
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
        Map<String, Object> myData = client.post().uri(uri).bodyValue(m).retrieve().bodyToMono(Map.class)
                .block();

        String token = String.valueOf(myData.get("accessToken"));
        return token;
    }

    @Override
    public GeneralAbhaResponse generateOTP(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getAadhaar())) {
            throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_NUMBER.getCode(), ErrorCode.INVALID_AADHAAR_NUMBER.getMessage());
        }
        request.setAadhaar(Helper.decryptData(request.getAadhaar()));

        abhaApiUtils.checkValidation(request.getAadhaar(), FieldType.AADHAR_CARD);
        abhaApiUtils.checkValidation(String.valueOf(request.isConsent()), FieldType.CONSENT);
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String aadhaarEncryptValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getAadhaar())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .scope(Collections.singletonList(AbhaScope.ABHA_ENROL.getScope()))
                .loginHint("aadhaar")
                .otpSystem("aadhaar")
                .loginId(aadhaarEncryptValue).build();


        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/enrollment/request/otp")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();
        return mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse);
    }

    @Override
    public GeneralAbhaResponse verifyOTP(AbhaRegistrationRequest request) {
        AbhaOTPVerificationResponse abhaOTPVerificationResponse = abhaFetchDetails.getAbhaAuthDetails(request);

        boolean isNew = abhaOTPVerificationResponse.getIsNew();
        String xToken = abhaOTPVerificationResponse.getTokens().getToken();


        request.setAbhaToken(xToken);

//        GeneralAbhaResponse pngUrl = abhaFetchDetails.fetchAbhaCard(request);
//        abhaFetchDetails.fetchAbhaQR(request);
//        abhaFetchDetails.fetchAbhaPdf(request);
        abhaOTPVerificationResponse.setRequestMobile(request.getMobile());
        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse);
//        response.setPreSignedUrl(pngUrl.getPreSignedUrl());
        response.setNew(isNew);
        response.setAuthType("v3");
        return response;
    }


    @Override
    public GeneralAbhaResponse generateMobileOTP(AbhaRegistrationRequest request) {

        if (ObjectUtils.isEmpty(request.getMobile())) {
            throw new WishFoundationException(ErrorCode.INVALID_PHONE_NUMBER.getCode(), ErrorCode.INVALID_PHONE_NUMBER.getMessage());
        }
        abhaApiUtils.checkValidation(request.getMobile(), FieldType.MOBILE);
        abhaApiUtils.checkValidation(EnvironmentConfig.OTP_PREFIX + request.getTemplateKey(), FieldType.OTP_RATE_LIMIT);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();
        String mobileEncryptValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getMobile())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());


        AbhaOTPEnrolRequest abhaOTPEnrolRequest = AbhaOTPEnrolRequest.builder()
                .txnId(request.getTxnId())
                .scope(Arrays.asList(AbhaScope.ABHA_ENROL.getScope(), AbhaScope.MOBILE_VERIFY.getScope()))
                .loginHint("mobile")
                .otpSystem("abdm")
                .loginId(mobileEncryptValue).build();


        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/enrollment/request/otp")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .bodyValue(abhaOTPEnrolRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();

        return mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse);
    }

    @Override
    public GeneralAbhaResponse verifyMobileOTP(AbhaRegistrationRequest request) {
        if (ObjectUtils.isEmpty(request.getOtp())) {
            throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }

        abhaApiUtils.checkValidation(request.getOtp(), FieldType.OTP);

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        String otpEncryptValue = Helper.getEncryptedValue(EncryptKeyRequest.builder().keyToEncrypt(request.getOtp())
                .publicKey(EnvironmentConfig.ABHA_V3_PUBLIC_KEY)
                .cipherType(CipherType.RSA_ECB_OAEPWithSHA_1AndMGF1Padding.getCipherType()).build());

        AbhaOTPVerificationRequest abhaOTPVerificationRequest = AbhaOTPVerificationRequest.builder()
                .scope(Arrays.asList(AbhaScope.ABHA_ENROL.getScope(), AbhaScope.MOBILE_VERIFY.getScope()))
                .authData(AuthData.builder()
                        .authMethods(List.of("otp"))
                        .otp(Otp.builder()
                                .timeStamp(Helper.getSimpleTimeStamp())
                                .txnId(request.getTxnId())
                                .otpValue(otpEncryptValue)
                                .build())
                        .build())
                .build();

        AbhaOTPVerificationResponse abhaOTPVerificationResponse = client.post()
                .uri("/v3/enrollment/auth/byAbdm")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .bodyValue(abhaOTPVerificationRequest)
                .retrieve()
                .bodyToMono(AbhaOTPVerificationResponse.class)
                .block();
        if (abhaOTPVerificationResponse.getAuthResult().equalsIgnoreCase("Failed")) {
            throw new WishFoundationException(ErrorCode.INVALID_MOBILE_OTP.getCode(), ErrorCode.INVALID_MOBILE_OTP.getMessage(), HttpStatus.BAD_REQUEST);
        }
        GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(abhaOTPVerificationResponse);
        return response;

    }

    public GeneralAbhaResponse mapABHAResponseToGeneralResponse(AbhaOTPVerificationResponse responseData) {
        String abhaNumber = "";
        String mobile = "";
        String name = "";
        String txnId = Optional.ofNullable(responseData.getTxnId()).orElse("");
        String message = Optional.ofNullable(responseData.getMessage()).orElse("");

        if (!ObjectUtils.isEmpty(responseData.getAbhaProfile())) {
            abhaNumber = Optional.ofNullable(responseData.getAbhaProfile().getAbhaNumber()).orElse("");
            mobile = Optional.ofNullable(responseData.getAbhaProfile().getMobile()).orElse("");
            name = responseData.getAbhaProfile().getFirstName();
        }
        boolean mobileNumberMatched = mobile.equalsIgnoreCase(responseData.getRequestMobile());

        return GeneralAbhaResponse.builder()
                .txnId(txnId)
                .message(message)
                .abhaNumber(abhaNumber)
                .tokens(responseData.getTokens())
                .firstName(name)
                .mobile(mobile)
                .mobileNumberMatched(mobileNumberMatched)
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

    @Override
    public ResponseEntity<Void> saveAbhaDetails(AbhaOTPVerificationResponse abhaOTPVerificationResponse) {

        YatriPulseUsers yatriPulseUsers = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(UserContext.getCurrentUserName(), UserContext.getCurrentPhoneNumber());
        if (ObjectUtils.isEmpty(yatriPulseUsers))
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage());

        ABHAUserDetails abhaUserDetails = new ABHAUserDetails();
        ABHAProfile abhaProfileRequest = abhaOTPVerificationResponse.getAbhaProfile();
        if (abhaProfileRequest.getAbhaNumber() != null)
            abhaUserDetails.setAbhaNumber(Helper.encrypt(abhaProfileRequest.getAbhaNumber()));

        Helper.updateFieldIfNotNull(abhaUserDetails::setFirstName, abhaProfileRequest.getFirstName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setMiddleName, abhaProfileRequest.getMiddleName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setLastName, abhaProfileRequest.getLastName());
        Helper.updateFieldIfNotNull(abhaUserDetails::setDateOfBirth, abhaProfileRequest.getDob());
        Helper.updateFieldIfNotNull(abhaUserDetails::setGender, abhaProfileRequest.getGender());

//TODO : need to save profile photo in s3 and save path to pathfolder

        Helper.updateFieldIfNotNull(abhaUserDetails::setPhoneNumber, abhaProfileRequest.getMobile());
        Helper.updateFieldIfNotNull(abhaUserDetails::setEmailId, abhaProfileRequest.getEmail());

        if (abhaProfileRequest.getPhrAddress() != null)
            abhaUserDetails.setPhrAddress(Helper.encryptList(abhaProfileRequest.getPhrAddress()));


        Helper.updateFieldIfNotNull(abhaUserDetails::setAddress, abhaProfileRequest.getAddress());
        Helper.updateFieldIfNotNull(abhaUserDetails::setDistrictCode, abhaProfileRequest.getDistrictCode());
        Helper.updateFieldIfNotNull(abhaUserDetails::setStateCode, abhaProfileRequest.getStateCode());
        Helper.updateFieldIfNotNull(abhaUserDetails::setPinCode, abhaProfileRequest.getPinCode());
        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaType, abhaProfileRequest.getAbhaType());
        Helper.updateFieldIfNotNull(abhaUserDetails::setAbhaStatus, abhaProfileRequest.getAbhaStatus());
        abhaUserDetails.setFullName();

        yatriPulseUsers.setAbhaUserId(abhaUserDetailsRepo.save(abhaUserDetails).getId());
        yatriPulseUsersRepo.save(yatriPulseUsers);

        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    public ABHAProfile fetchAbhaDetails(ABHAUserRequest abhaUserRequest) {

        Optional<ABHAUserDetails> abhaUserDetails = abhaUserDetailsRepo.findByAbhaNumber(abhaUserRequest.getAbhaProfile().getAbhaNumber());

        if (abhaUserDetails.isEmpty())
            throw new WishFoundationException(ErrorCode.ABHA_USER_DETAILS_IS_NOT_PRESENT.getCode(),
                    ErrorCode.ABHA_USER_DETAILS_IS_NOT_PRESENT.getMessage());
        ABHAProfile abhaProfile = Helper.MAPPER.convertValue(abhaUserDetails.get(), ABHAProfile.class);
        abhaProfile.setPhrAddress(Helper.decryptList(abhaProfile.getPhrAddress()));
        abhaProfile.setAbhaNumber(Helper.decrypt(abhaProfile.getAbhaNumber()));
        return abhaProfile;
    }

    public GetABHAResponse fetchAbhaProfile(AbhaRegistrationRequest request) {
        String token = "";
        if (!StringUtils.hasLength(request.getAbhaToken()))
            token = abhaFetchDetails.getAbhaAuthDetails(request).getTokens().getToken();
        else
            token = request.getAbhaToken();
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();

        GetABHAResponse profile = client.get()
                .uri("/v3/profile/account")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("X-token", "Bearer " + token)
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GetABHAResponse.class)
                .block();
        return profile;
    }

    public GeneralAbhaResponse addressSuggestion(AbhaRegistrationRequest request) {
        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();
        GeneralAbhaResponse response = client.get()
                .uri("/v3/enrollment/enrol/suggestion")
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Transaction_id", request.getTxnId())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GeneralAbhaResponse.class)
                .block();

        return response;
    }

    public GeneralAbhaResponse createAddress(AbhaRegistrationRequest request) {

        String currentUserName = UserContext.getCurrentUserName();

        WebClient client = webClient.baseUrl(env.abhaRegistrationHost).build();
        GeneralAbhaResponse response = client.post()
                    .uri("/v3/enrollment/enrol/abha-address")
                .bodyValue(request)
                .header("Authorization", "Bearer " + generateGatewayToken())
                .header("TIMESTAMP", Helper.getIsoTimeStamp())
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .retrieve()
                .bodyToMono(GeneralAbhaResponse.class)
                .block();

        // TODO CHANGE FOR PROD.
        if (!request.getAbhaAddress().contains("@sbx")) {
            request.setAbhaAddress(request.getAbhaAddress() + "@sbx");
        }

        YatriPulseUsers userByUserName = yatriPulseUsersRepo.findUserByUserName(currentUserName).get();
        ABHAUserDetails aBHAUserDetails = abhaUserDetailsRepo.findById(userByUserName.getAbhaUserId()).get();
        aBHAUserDetails.setPhrAddress(Helper.encryptList(Arrays.asList(request.getAbhaAddress())));
        abhaUserDetailsRepo.save(aBHAUserDetails);

        return null;
    }

    public GeneralAbhaResponse createAbhaIdByDemo(AbhaRegistrationRequest request) {
        request.setAadharNumber(Helper.decryptData(request.getAadharNumber()));
        abhaApiUtils.checkValidation(request.getAadharNumber(), FieldType.AADHAR_CARD);
        abhaApiUtils.checkValidation(request.getMobileNumber(), FieldType.MOBILE);
        abhaApiUtils.checkValidation(String.valueOf(request.isConsent()), FieldType.CONSENT);
        try {
            HealthModel hm = new HealthModel();
            hm.setStateName(request.getStateCode());
            // GETTING state code in response.
            hm = healthUtils.getStateCode(hm);
            // GETTING disrict code in response.
            hm.setDistrictName(request.getDistrictCode());
            hm = healthUtils.getDistrictCode(hm);

            request.setStateCode(hm.getStateCode());
            request.setDistrictCode(hm.getDistrictCode());
        } catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }

        try {
        WebClient client = webClient.baseUrl("https://healthid.abdm.gov.in").build();
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

            // SAVING DETAIL IN DB.
            abhaVerificationServiceImpl.saveAbhaDetails(profile);

//            request.setABHANumber(profile.getABHANumber());
//            request.setFirstName(profile.getFirstName());
//            request.setXToken(profile.getJwtResponse().getToken());
//            abhaFetchDetails.fetchAbhaPdfV2(request);
//            GeneralAbhaResponse pngUrl = abhaFetchDetails.fetchAbhaCardV2(request);
//            abhaFetchDetails.fetchAbhaQRV2(request);

            GeneralAbhaResponse response = mapABHAResponseToGeneralResponse(profile, request);
//            response.setPreSignedUrl(pngUrl.getPreSignedUrl());
            response.setTokens(profile.getJwtResponse());
            response.setAuthType("v2");
            return response;
        }catch (Exception e){
            throw new WishFoundationException(ErrorCode.IDTP_AADHAR_DATA_MISMATCH.getCode(), ErrorCode.IDTP_AADHAR_DATA_MISMATCH.getMessage());
        }

    }

    @Override
    public ResponseEntity<ABHAUserDetails> saveABHAUserDetails(AbhaRegistrationRequest request) {
        GetABHAResponse getABHAResponse = fetchAbhaProfile(request);
        return abhaVerificationServiceImpl.saveAbhaDetails(getABHAResponse);
    }


    //    // TODO delete this method after implementation.
//    @GetMapping("/test/notification")
//    public String getNotify(){
//        System.out.println("----------------HELLO-------------------");
//        NotificationRequestModel requestModel = new NotificationRequestModel();
//        requestModel.setMessageBody(EnvironmentConfig.USER_SIGN_UP_TEMPLATE
//                .replaceAll(EnvironmentConfig.USER_NAME_MARCO , "ANKIT SINGH")
//                .replaceAll(EnvironmentConfig.OTP_MARCO,"90909"));
//        requestModel.setPhoneNumber("+917531960516");
//
//
////          notificationUtils.sendSMS(requestModel);
//        requestModel.setEmailTo(new HashSet<>(Arrays.asList("iankit.singh@outlook.com")));
//        requestModel.setSubject(EnvironmentConfig.USER_SIGN_UP_SUBJECT);
//
//          return notificationUtils.sendEmail(requestModel);
//    }


//    @Override
//    public AbhaUserDetailsResponse createHealthIdByAadhaar(AbhaRegistrationRequest request) {
//        WebClient client = webClient.baseUrl(ABHA_REGISTRATION_API_HOST).build();
//        AbhaRegistrationRequest abhaRegistrationRequest = request.toBuilder().consent(true).consentVersion("v1.0").build();
//        AbhaUserDetailsResponse myData = client.post()
//                .uri("/api/v2/registration/aadhaar/createHealthIdByAdhaar")
//                .header("Authorization", "Bearer " +generateGatewayToken())
//                .bodyValue(abhaRegistrationRequest)
//                .retrieve()
//                .bodyToMono(AbhaUserDetailsResponse.class)
//                .onErrorMap(ex -> new RuntimeException("Failed with an error", ex))
//                .block();
//
////        System.out.println(myData);
////        AbhaUserDetailsResponse m = new AbhaUserDetailsResponse();
//
//
//        return myData;
//    }


}
