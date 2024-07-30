package org.wishfoundation.healthservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.chardhamcore.config.UserContext;
import org.wishfoundation.chardhamcore.security.JwtRequestFilter;
import org.wishfoundation.healthservice.request.DocumentsPathRequest;
import org.wishfoundation.healthservice.request.YatriPulseUserRequest;
import org.wishfoundation.healthservice.response.MedicalsReportsResponse;
import org.wishfoundation.healthservice.utils.EnvironmentConfig;
import org.wishfoundation.healthservice.utils.UserServiceEndPoints;

import java.util.List;

/**
 * This class provides utility methods for interacting with the Yatri service.
 * It uses Spring's WebClient to make HTTP requests to the user service.
 */
@Component
@RequiredArgsConstructor
public class YatriServiceUtils {
    /**
     * The WebClient builder for creating instances of WebClient.
     */
    private final  WebClient.Builder webClient;

    /**
     * The environment configuration for fetching the user service base URL.
     */
    private final EnvironmentConfig environmentConfig;

    /**
     * A constant for the internal request header.
     */
    public static final String INTERNAL_REQUEST = "x-internal-request";

    /**
     * Updates the yatri's details in the user service.
     *
     * @param request The request containing the updated yatri details.
     */
    public void updateYatriDetails(YatriPulseUserRequest request){
        WebClient client = webClient.baseUrl(environmentConfig.getUserService()).build();
        client.post()
                .uri(UserServiceEndPoints.UPDATE_USER_DETAILS)
                .header("Authorization", "Bearer " + UserContext.getCurrentToken())
                .header("Content-Type", "application/json")
                .header("X-Organization-Id",UserContext.getCurrentOrganization())
                .header(INTERNAL_REQUEST, "true")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


    /**
     * Fetches the medical records of the yatri from the user service.
     *
     * @return The medical records of the yatri.
     */
    public MedicalsReportsResponse fetchMedicalRecords(){
        WebClient client = webClient.baseUrl(environmentConfig.getUserService()).build();
        return client.get().uri(UserServiceEndPoints.FETCH_MEDICAL_REPORTS)
                .header("Authorization", "Bearer " + UserContext.getCurrentToken())
                .header("X-Organization-Id",UserContext.getCurrentOrganization())
                .header(INTERNAL_REQUEST, "true")
                .header("Content-Type", "application/json").retrieve()
                .bodyToMono(MedicalsReportsResponse.class).block();
    }

    /**
     * Deletes the medical documents of the yatri from the user service.
     *
     * @param documentsPathRequestList The list of document paths to be deleted.
     */
    public void deleteMedicalDocument(List<DocumentsPathRequest> documentsPathRequestList){
        WebClient client = webClient.baseUrl(environmentConfig.getUserService()).build();
        client.patch()
                .uri(UserServiceEndPoints.DELETE_MEDICAL_DOCUMENTS)
                .header("Authorization", "Bearer " + UserContext.getCurrentToken())
                .header("X-Organization-Id",UserContext.getCurrentOrganization())
                .header(INTERNAL_REQUEST, "true")
                .header("Content-Type", "application/json")
                .bodyValue(documentsPathRequestList)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
