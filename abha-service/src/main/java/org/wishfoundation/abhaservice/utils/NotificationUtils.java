package org.wishfoundation.abhaservice.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.abhaservice.exception.WishFoundationException;
import org.wishfoundation.abhaservice.request.BaseDiscoveryRequest;
import org.wishfoundation.abhaservice.request.NotificationRequestModel;
import org.wishfoundation.abhaservice.response.yatri.YatriPulseUserResponse;

@Component
public class NotificationUtils {

    public static final String NOTIFICATION_SERVICE_HOST = StringUtils.hasLength(System.getenv("NOTIFICATION_SERVICE_HOST")) ? System.getenv("NOTIFICATION_SERVICE_HOST") : "http://yatripulse-dev.centilytics.com/notification-service";
    @Autowired
    private WebClient.Builder webClient;
    public static final String INTERNAL_REQUEST = "x-internal-request";
    public  String sendSMS(NotificationRequestModel requestModel){
        System.out.println("SMS_BASE_HOST : "+NOTIFICATION_SERVICE_HOST);
        WebClient client = webClient.baseUrl(NOTIFICATION_SERVICE_HOST).build();

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
}
