package org.wishfoundation.iomtdeviceinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.iomtdeviceinventory.request.IoMtLoginRequest;
import org.wishfoundation.iomtdeviceinventory.response.IotMtLoginResponse;
import org.wishfoundation.iomtdeviceinventory.response.IotResponse;
import org.wishfoundation.iomtdeviceinventory.service.IoMtServiceImpl;


/**
 * Controller for handling IoT MedTech (IoMT) related requests.
 */
@RestController
@RequestMapping("/api/iomt/")
@RequiredArgsConstructor
public class IoMtController {

    /**
     * IoMT service implementation.
     */
    private final IoMtServiceImpl ioMtService;

    /**
     * Handles IoMT login request.
     *
     * @param ioMtLoginRequest The request containing login credentials.
     * @return The response containing the login token.
     */
    @PostMapping("login")
    public IotMtLoginResponse iomtLogin(@RequestBody IoMtLoginRequest ioMtLoginRequest) {
        return ioMtService.iomtLoginToken(ioMtLoginRequest);
    }

    /**
     * Verifies the token for the given entity and communication address.
     *
     * @param token The token to be verified.
     * @param entityid The entity ID.
     * @param communicationaddr The communication address.
     * @return The response indicating the verification result.
     */
    @GetMapping("verifyToken")
    public IotResponse iomtVerifyToken(@RequestHeader("apptoken") String token, @RequestHeader("entityid") String entityid, @RequestHeader("communicationaddr") String communicationaddr) {
        return ioMtService.iomtVerifyToken(token, entityid, communicationaddr);
    }
}

