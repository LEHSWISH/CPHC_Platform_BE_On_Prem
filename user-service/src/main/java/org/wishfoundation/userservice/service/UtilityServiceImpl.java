package org.wishfoundation.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.response.PinCodeResponse;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.Helper;

/**
 * This class implements the UtilityService interface and provides methods for utility operations.
 */
@RequiredArgsConstructor
@Service
public class UtilityServiceImpl implements UtilityService {

    /**
     * The WebClient.Builder instance for making HTTP requests.
     */
    private final WebClient.Builder webClient;

    /**
     * This method retrieves the district, state, and city information for a given pin code.
     *
     * @param pinCode The pin code for which to retrieve the information.
     * @return A PinCodeResponse object containing the district, state, and city information.
     * @throws WishFoundationException If the pin code is invalid or if an error occurs while making the HTTP request.
     */
    @Override
    public PinCodeResponse getInfoByPinCode(String pinCode) {
        // Create a WebClient instance with the base URL of the pin code API
        WebClient client = webClient.baseUrl(EnvironmentConfig.PIN_CODE_API_HOST).build();

        // Validate the pin code length
        if (pinCode.length() > 6) {
            throw new WishFoundationException(ErrorCode.INVALID_PIN_CODE.getCode(), ErrorCode.INVALID_PIN_CODE.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Make an HTTP GET request to the pin code API
        String response = client.get()
                .uri(pinCode)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            // Parse the JSON response
            JsonNode jsonNode = Helper.MAPPER.readTree(response);

            // Check if the response contains valid data
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstNode = jsonNode.get(0);
                if (firstNode.has("PostOffice") && firstNode.get("PostOffice").isArray() && firstNode.get("PostOffice").size() > 0) {
                    // Extract the district, state, and city information from the response
                    JsonNode postOffice = firstNode.get("PostOffice").get(0);
                    return PinCodeResponse.builder()
                            .district(postOffice.get("District").asText())
                            .state(postOffice.get("State").asText())
                            .city(postOffice.get("Block").asText())
                            .build();
                }
            }
        } catch (JsonProcessingException e) {
            // Handle JSON processing exceptions
            throw new WishFoundationException(e.getLocalizedMessage());
        }

        // Return an empty PinCodeResponse if no valid data is found
        return PinCodeResponse.builder().build();
    }
}
