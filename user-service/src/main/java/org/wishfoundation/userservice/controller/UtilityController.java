package org.wishfoundation.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.userservice.response.PinCodeResponse;
import org.wishfoundation.userservice.service.UtilityServiceImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/utility")
public class UtilityController {

    private final UtilityServiceImpl utilityService;


    @GetMapping(path = "/pinCode/{pin_code}")
    public PinCodeResponse getInfoByPinCode(@PathVariable("pin_code") String pinCode){
       return  utilityService.getInfoByPinCode(pinCode);
    }
}
