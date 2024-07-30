package org.wishfoundation.userservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.request.health.HealthModel;
import org.wishfoundation.userservice.security.JwtRequestFilter;

/**
 * Utility class for interacting with the Health Service.
 * This class provides methods to retrieve state and district codes from the Health Service.
 */
@RequiredArgsConstructor
@Component
public class HealthUtils {

    /**
     * Autowired instance of WebClient.Builder for creating WebClient instances.
     */
    @Autowired
    private  WebClient.Builder webClient;

    /**
     * Instance of EnvironmentConfig for accessing environment-specific configurations.
     */
    private final EnvironmentConfig environmentConfig;

    /**
     * Method to retrieve state code from the Health Service.
     *
     * @param requestModel The request model containing the state name.
     * @return The HealthModel containing the state code.
     */
    public HealthModel getStateCode(HealthModel requestModel){

        System.out.println("HEALTH_BASE_HOST : "+environmentConfig.getHealthService());
        WebClient client = webClient.baseUrl(environmentConfig.getHealthService()).build();
        HealthModel myData = client.post()
                .uri("/api/v1/state/get-code")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
//                .header("X-Organization-Id",UserContext.getCurrentOrganization())
//                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(HealthModel.class)
                .block();
        return myData;
    }

    /**
     * Method to retrieve district code from the Health Service.
     *
     * @param requestModel The request model containing the district name.
     * @return The HealthModel containing the district code.
     */
    public HealthModel getDistrictCode(HealthModel requestModel){
//        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_PREFIX + UserContext.getCurrentToken();
//        System.out.println("currentToken : "+ currentToken);

        System.out.println("HEALTH_BASE_HOST : "+environmentConfig.getHealthService());
        WebClient client = webClient.baseUrl(environmentConfig.getHealthService()).build();
        HealthModel myData = client.post()
                .uri("/api/v1/district/get-code")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
//                .header("X-Organization-Id",UserContext.getCurrentOrganization())
//                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .bodyValue(requestModel)
                .retrieve()
                .bodyToMono(HealthModel.class)
                .block();
        return myData;
    }
}
