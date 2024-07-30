package org.wishfoundation.userservice.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.enums.FieldType;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.response.OtpResponse;
import org.wishfoundation.userservice.security.JWTService;
import org.wishfoundation.userservice.security.JwtRequestFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.wishfoundation.userservice.utils.EnvironmentConfig.*;

@RequiredArgsConstructor
@Component
public class AbhaApiUtils {

//    public static final String ABHA_SERVICE_HOST = StringUtils.hasLength(System.getenv("ABHA_SERVICE_HOST")) ? System.getenv("ABHA_SERVICE_HOST") : "http://localhost:8083";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JWTService jwtService;
    // FOR M2
    @Autowired
    private WebClient.Builder webClient;

    private final EnvironmentConfig environmentConfig;

    public void checkValidation(String fieldValue, FieldType fieldType) throws WishFoundationException {
        boolean isValid = false;
        if (ObjectUtils.isEmpty(fieldValue)) {
            throwExceptionForDocumentType(fieldType);
        }

        switch (fieldType) {
            case PASSPORT:
                isValid = fieldValue.matches("^[A-PR-WY-Z][1-9]\\d\\d{4}[1-9]$");
                break;
            case PAN_CARD:
                isValid = fieldValue.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
                break;
            case AADHAR_CARD:
                isValid = fieldValue.matches("^[2-9]\\d{3}\\d{4}\\d{4}$");
                break;
            case VOTER_ID_CARD:
                isValid = fieldValue.matches("^[A-Z]{3}\\d{7}$") || fieldValue.matches("^[A-Z]{3}[0-9]{7}$");
                break;
            case DRIVING_LICENSE:
                isValid = fieldValue.matches("^(([A-Z]{2}[0-9]{2})|([A-Z]{2}-[0-9]{2}))((19|20)[0-9][0-9])[0-9]{7}$");
                break;
            case OTP:
                isValid = fieldValue.matches("^[0-9]{6}$");
                break;
            case MOBILE:
                isValid = fieldValue.matches("^[0-9]{10}$");
                break;
            case CONSENT:
                isValid = Boolean.valueOf(fieldValue);
                break;
            case OTP_RATE_LIMIT:
                isValid = checkAbhaRateLimit(fieldValue).isRateLimitExceed();
                break;
            default:
                break;
        }
        if (!isValid) {
            throwExceptionForDocumentType(fieldType);
        }
    }

    private void throwExceptionForDocumentType(FieldType governmentIdType) throws WishFoundationException {
        switch (governmentIdType) {
            case PASSPORT:
                throw new WishFoundationException(ErrorCode.INVALID_PASSPORT.getCode(), ErrorCode.INVALID_PASSPORT.getMessage());
            case PAN_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_PAN_CARD.getCode(), ErrorCode.INVALID_PAN_CARD.getMessage());
            case AADHAR_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_NUMBER.getCode(), ErrorCode.INVALID_AADHAAR_NUMBER.getMessage());
            case VOTER_ID_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_VOTER_ID.getCode(), ErrorCode.INVALID_VOTER_ID.getMessage());
            case DRIVING_LICENSE:
                throw new WishFoundationException(ErrorCode.INVALID_DRIVING_LICENSE.getCode(), ErrorCode.INVALID_DRIVING_LICENSE.getMessage());
            case OTP:
                throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
            case MOBILE:
                throw new WishFoundationException(ErrorCode.INVALID_PHONE_NUMBER.getCode(), ErrorCode.INVALID_PHONE_NUMBER.getMessage());
            case TXT_ID:
                throw new WishFoundationException(ErrorCode.INVALID_TRANSACTION_ID.getCode(), ErrorCode.INVALID_TRANSACTION_ID.getMessage());
            case CONSENT:
                throw new WishFoundationException(ErrorCode.UNCONSENT_REQUEST.getCode(), ErrorCode.UNCONSENT_REQUEST.getMessage());
            case OTP_RATE_LIMIT:
                throw new WishFoundationException(ErrorCode.OTP_RATE_LIMIT.getCode(), ErrorCode.OTP_RATE_LIMIT.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
            default:
                throw new WishFoundationException(ErrorCode.INVALID_GOVERNMENT_ID.getCode(), ErrorCode.INVALID_GOVERNMENT_ID.getMessage());
        }
    }

    public OtpResponse checkAbhaRateLimit(String rateLimitKey) {

        //TODO : Remove this code when its going to staging or prod
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList("9896870412", "9696322446", "7206592762", "8448252500"));
        try {
            boolean tempPhoneNumberFlag = tempList.contains(UserContext.getCurrentPhoneNumber());
            if (tempPhoneNumberFlag) {
                Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey, 0); // Check current value without
                if (currentCount == null) {
                    redisTemplate.opsForValue().set(rateLimitKey, 1);
                    currentCount = 1L;
                } else if (currentCount <= 100) {
                    redisTemplate.opsForValue().increment(rateLimitKey, 1);
                    currentCount++;
                }
                redisTemplate.expire(rateLimitKey, ABHA_RATE_LIMIT_PERIOD_SECONDS, TimeUnit.SECONDS);
                return OtpResponse.builder().rateLimitExceed(currentCount <= 100).attemptLeft(currentCount).build();
            }
        } catch (Exception e) {

        }
        //===============================================================================================================
        rateLimitKey = rateLimitKey + "_" + UserContext.getCurrentUserName();

        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey, 0); // Check current value without
        if (currentCount == null) {
            redisTemplate.opsForValue().set(rateLimitKey, 1);
            currentCount = 1L;
        } else if (currentCount <= RATE_LIMIT) {
            redisTemplate.opsForValue().increment(rateLimitKey, 1);
            currentCount++;
        }
        redisTemplate.expire(rateLimitKey, ABHA_RATE_LIMIT_PERIOD_SECONDS, TimeUnit.SECONDS);
        return OtpResponse.builder().rateLimitExceed(currentCount <= ABHA_RATE_LIMIT).attemptLeft(currentCount).build();
    }

    public void uploadDocuments(YatriPulseUserRequest requestModel) {
        System.out.println("HEALTH_BASE_HOST : " + environmentConfig.getAbhaService());
        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_PREFIX + UserContext.getCurrentToken();
        System.out.println("currentToken : "+ currentToken);

        WebClient client = webClient.baseUrl(environmentConfig.getAbhaService()).build();
        client.post()
                .uri("/api/v1/abha-hip/auth/init")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .header("X-Organization-Id", UserContext.getCurrentOrganization())
                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public void notifyUser(YatriPulseUserRequest requestModel) {
        System.out.println("HEALTH_BASE_HOST : " + environmentConfig.getAbhaService());
        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_PREFIX + UserContext.getCurrentToken();
        System.out.println("currentToken : " + currentToken);

        try {
            System.out.println("notifyUser : " + Helper.MAPPER.writeValueAsString(requestModel));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        WebClient client = webClient.baseUrl(environmentConfig.getAbhaService()).build();
        client.post()
                .uri("/api/v1/abha-hip/patients/sms/notify")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .header("X-Organization-Id", UserContext.getCurrentOrganization())
                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}
