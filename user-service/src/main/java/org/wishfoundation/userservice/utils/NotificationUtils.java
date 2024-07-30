package org.wishfoundation.userservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.request.notification.NotificationRequestModel;
import org.wishfoundation.userservice.security.JwtRequestFilter;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.Map;

/**
 * Utility class for sending notifications through various channels like SMS, SMTP, and Email.
 * This class uses Spring's WebClient for making HTTP requests to the notification service.
 */
@RequiredArgsConstructor
@Component
public class NotificationUtils {

    /**
     * Spring's WebClient builder for creating instances of WebClient.
     */
    @Autowired
    private  WebClient.Builder webClient;

    /**
     * Instance of EnvironmentConfig for accessing configuration properties.
     */
    private final EnvironmentConfig environmentConfig;

//    public static final String SMS_BASE_HOST = StringUtils.hasLength(System.getenv("SMS_BASE_HOST")) ? System.getenv("SMS_BASE_HOST") : "http://localhost:8081";

    /**
     * Map containing template details for different types of OTP notifications.
     */
    public static final Map<String, String> OTP_TEMPLATE_MAP = ImmutableMap.<String, String>builder()
            .put("sign-up", EnvironmentConfig.USER_SIGN_UP_OTP_SUBJECT+"#"+EnvironmentConfig.USER_SIGN_UP_OTP_TEMPLATE)
            .put("reset-password", EnvironmentConfig.RESET_PASSWORD_SUBJECT+"#"+EnvironmentConfig.RESET_PASSWORD_TEMPLATE)
            .put("forget-username", EnvironmentConfig.RECOVER_USERNAME_SUBJECT+"#"+EnvironmentConfig.RECOVER_USERNAME_TEMPLATE)
            .put("yatri-phone-number", EnvironmentConfig.UPDATE_CONTACT_DETAILS_SUBJECT+"#"+EnvironmentConfig.UPDATE_CONTACT_DETAILS_TEMPLATE).build();

    /**
     * Sends an SMS notification using the notification service.
     *
     * @param requestModel The request model containing details of the notification to be sent.
     * @return The response from the notification service.
     */
    public  String sendSMS(NotificationRequestModel requestModel){
        System.out.println("SMS_BASE_HOST : "+environmentConfig.getNotificationService());
        WebClient client = webClient.baseUrl(environmentConfig.getNotificationService()).build();

//        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_BASIC_PREFIX + UserContext.getCurrentToken();
//        System.out.println("currentToken : "+ currentToken);
        String myData = client.post()
                .uri("/api/v1/sms/send-sms")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
//                .header("X-Organization-Id",UserContext.getCurrentOrganization())
//                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(ex -> new RuntimeException("Failed with an error", ex))
                .block();
        return myData;
    }

    /**
     * Sends an SMS notification using SMTP through the notification service.
     *
     * @param requestModel The request model containing details of the notification to be sent.
     * @return The response from the notification service.
     */
    public  String sendSMSsmtp(NotificationRequestModel requestModel){
        System.out.println("SMS_BASE_HOST : "+environmentConfig.getNotificationService());
        WebClient client = webClient.baseUrl(environmentConfig.getNotificationService()).build();
//        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_BASIC_PREFIX + UserContext.getCurrentToken();
//        System.out.println("currentToken : "+ currentToken);
        String myData = client.post()
                .uri("/api/v1/sms/smtp/notify-number")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
//                .header("X-Organization-Id",UserContext.getCurrentOrganization())
//                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(ex -> new RuntimeException("Failed with an error", ex))
                .block();
        return myData;
    }


    /**
     * Sends an email notification using the notification service.
     *
     * @param requestModel The request model containing details of the notification to be sent.
     * @return The response from the notification service.
     */
    public  String sendEmail(NotificationRequestModel requestModel){
        WebClient client = webClient.baseUrl(environmentConfig.getNotificationService()).build();

//        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_BASIC_PREFIX + UserContext.getCurrentToken();
        String myData = client.post()
                .uri("/api/v1/email/send-email")
                .header("Content-Type", "application/json")
//                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .header("Accept", "*/*")
//                .header("X-Organization-Id",UserContext.getCurrentOrganization())
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(ex -> new RuntimeException("Failed with an error", ex))
                .block();
        return myData;
    }
}
