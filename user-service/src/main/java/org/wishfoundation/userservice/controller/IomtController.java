package org.wishfoundation.userservice.controller;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.wishfoundation.userservice.request.IotRequest;
import org.wishfoundation.userservice.response.IotResponse;
import org.wishfoundation.userservice.service.IomtService;

/**
 * This class is a controller for handling IOT related requests.
 * It provides endpoints for verifying tokens, getting user profiles, injecting data, and validating tokens.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/iomt/")
public class IomtController {

    /**
     * Service for handling IOT related operations.
     */
    private final IomtService iomtService;

    /**
     * Endpoint for verifying tokens.
     *
     * @param token The token to be verified.
     * @param entityid The entity id associated with the token.
     * @param communicationaddr The communication address associated with the token.
     * @return An IotResponse indicating success or failure.
     */
    @GetMapping("verifyToken")
    public IotResponse iotVerifyToken(@RequestHeader("apptoken") String token, @RequestHeader("entityid") String entityid, @RequestHeader("communicationaddr") String communicationaddr) {
        //Implementation is required
        System.out.println("-token->" + token);
        System.out.println("-entityid->" + entityid);
        System.out.println("-communicationaddr->" + communicationaddr);
        return IotResponse.builder().code(200).message("Success").build();
    }

    /**
     * Endpoint for getting user profiles.
     *
     * @param iotRequest The request containing necessary parameters.
     * @return An IotResponse indicating success or failure.
     */
    @PostMapping("getProfile")
    public IotResponse getProfile(@RequestBody IotRequest iotRequest) {
        //Implementation is required
        System.out.println(iotRequest);
        return IotResponse.builder().code(200).message("Success").build();
    }

    /**
     * Endpoint for injecting data.
     *
     * @param iotRequest The request containing necessary parameters.
     * @return An IotResponse indicating success or failure.
     */
    @PostMapping("injectData")
    public IotResponse injectData(@RequestBody IotRequest iotRequest) {
        //Implementation is required
        System.out.println(iotRequest);
        return IotResponse.builder().code(200).message("Success").build();
    }

    /**
     * Endpoint for validating tokens.
     *
     * @param iotRequest The request containing necessary parameters.
     * @return An IotResponse indicating success or failure.
     */
    @PostMapping("validate-token")
    public IotResponse validateUser(@RequestBody IotRequest iotRequest) {
        return iomtService.validateUser(iotRequest);
    }
}
