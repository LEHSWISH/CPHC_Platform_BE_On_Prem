package org.wishfoundation.userservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.security.JwtRequestFilter;

@RequiredArgsConstructor
@Component
public class YatriUtils {

    @Autowired
    private WebClient.Builder webClient;

    private final EnvironmentConfig environmentConfig;


    private static final String INTERNAL_REQUEST = "x-internal-request";
    private static final String GET_TOURISM_DETAILS = "/api/v1/tourism/getUserInfoByIDTP/{id}";
    private static final String ORGANIZATION_ID = "X-Organization-Id";

    public void linkTourismThroughExcel(String tourismPortalId , String token){

        String currentToken = JwtRequestFilter.AUTH_HEADER_VAL_PREFIX + token;
        WebClient client = webClient.baseUrl(environmentConfig.getUserService()).build();

        client.get()
                .uri(uriBuilder -> uriBuilder
                                .path(GET_TOURISM_DETAILS )
                                .queryParam("consent", true)
                                .build(tourismPortalId))
                .header(JwtRequestFilter.AUTH_HEADER, currentToken)
                .header("Content-Type", "application/json")
                .header(ORGANIZATION_ID,UserContext.getCurrentOrganization())
                .header(INTERNAL_REQUEST, "true")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}