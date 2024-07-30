package org.wishfoundation.superadmin.utils;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.superadmin.request.EmailRequestModel;

@RequiredArgsConstructor
@Component
public class NotificationUtils {

    @Autowired
    private WebClient.Builder webClient;

    private final EnvironmentConfig environmentConfig;

    public  String sendEmail(EmailRequestModel requestModel){
        WebClient client = webClient.baseUrl(environmentConfig.getNotificationService()).build();

//        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_BASIC_PREFIX + UserContext.getCurrentToken();
        String myData = client.post()
                .uri("/api/v1/email/send-email-super-admin")
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
