package org.wishfoundation.abhaservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.abhaservice.config.EnvironmentConfig;
import org.wishfoundation.abhaservice.exception.WishFoundationException;
import org.wishfoundation.abhaservice.request.DocumentsPathRequest;
import org.wishfoundation.abhaservice.request.BaseDiscoveryRequest;
import org.wishfoundation.abhaservice.response.abha.ABHAUserDetails;
import org.wishfoundation.abhaservice.response.yatri.YatriPulseUserResponse;
import org.wishfoundation.chardhamcore.config.UserContext;
import org.wishfoundation.chardhamcore.security.JwtRequestFilter;

import static org.wishfoundation.chardhamcore.security.JwtRequestFilter.AUTH_HEADER_VAL_PREFIX;

@RequiredArgsConstructor
@Service
public class UserServiceUtils {

    private final EnvironmentConfig env;

//    private final String USER_SERVICE_HOST = .getUserServicenve();
    @Autowired
    private final WebClient.Builder webClient;

    public static final String INTERNAL_REQUEST = "x-internal-request";

    public ABHAUserDetails getAbhaDetails() {
        System.out.println("HEALTH_BASE_HOST : " + env.getUserService());
        WebClient client = webClient.baseUrl(env.getUserService()).build();
        String currentToken = AUTH_HEADER_VAL_PREFIX +  UserContext.getCurrentToken();
        System.out.println("currentToken : " + currentToken);
        try {
            ABHAUserDetails myData = client.post()
                    .uri("/api/v1/abha-detail")
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                    .header(INTERNAL_REQUEST, "true")
                    .retrieve()
                    .bodyToMono(ABHAUserDetails.class)
                    .block();
            return myData;
        }catch (Exception e){
            e.printStackTrace();
            throw new WishFoundationException("ABHA is pending");
        }
    }
    public String getMedicalCertificateBase64(DocumentsPathRequest request){
        WebClient client = webClient.baseUrl(env.getHealthService()).build();
        String currentToken = AUTH_HEADER_VAL_PREFIX +  UserContext.getCurrentToken();
        try{
            return client.post()
                    .uri("/api/v1/medical/get-medical-certificate-for-fhir")
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                    .header(INTERNAL_REQUEST, "true")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }catch (Exception e){
            e.printStackTrace();
            throw new WishFoundationException("ABHA is pending");
        }
    }

    public ABHAUserDetails getAbhaDetails(ABHAUserDetails request) {
        System.out.println("HEALTH_BASE_HOST : " + env.getUserService());
        WebClient client = webClient.baseUrl(env.getUserService()).build();
        String currentToken = AUTH_HEADER_VAL_PREFIX +  UserContext.getCurrentToken();
        System.out.println("currentToken : " + currentToken);
        try {
            ABHAUserDetails myData = client.post()
                    .uri("/api/v1/abha-detail/abha-id")
                    .bodyValue(request)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .header(INTERNAL_REQUEST, "true")
                    .retrieve()
                    .bodyToMono(ABHAUserDetails.class)
                    .block();
            return myData;
        }catch (Exception e){
            e.printStackTrace();
            throw new WishFoundationException("ABHA is pending");
        }
    }

    public YatriPulseUserResponse getYatriDetails(BaseDiscoveryRequest request) {
        System.out.println("HEALTH_BASE_HOST : " + env.getUserService());
        WebClient client = webClient.baseUrl(env.getUserService()).build();
        try {
            YatriPulseUserResponse myData = client.post()
                    .uri("/api/v1/abha-detail/user-detail")
                    .bodyValue(request)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .header(INTERNAL_REQUEST, "true")
                    .retrieve()
                    .bodyToMono(YatriPulseUserResponse.class)
                    .block();
            return myData;
        }catch (Exception e){
            e.printStackTrace();
            throw new WishFoundationException("Invaild User Details");
        }
    }

}
