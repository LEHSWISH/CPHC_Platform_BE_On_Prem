package org.wishfoundation.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.enums.RequestStatus;
import org.wishfoundation.userservice.response.CareGiverCareRecipientResponse;
import org.wishfoundation.userservice.response.LoginResponse;
import org.wishfoundation.userservice.service.CareGiverCareRecipientServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/v1/caregiver/")
public class CareGiverCareRecipientController {

    @Autowired
    private CareGiverCareRecipientServiceImpl careGiverCareRecipientService;

    @PostMapping("send-request/user-name/{user-name}")
    public ResponseEntity<Void> sendRequest(@PathVariable("user-name") String userName) {
        return careGiverCareRecipientService.sendRequest(userName);
    }

    @GetMapping("requests")
    public ResponseEntity<CareGiverCareRecipientResponse> viewCareGiverRequests() {
        return careGiverCareRecipientService.viewCareGiverRequests(UserContext.getUserId());
    }

    @GetMapping("care-giver-recipient")
    public ResponseEntity<CareGiverCareRecipientResponse> viewCareGiverRecipient() {
        return careGiverCareRecipientService.viewCareGiverRecipient(UserContext.getUserId());
    }

    @PostMapping("respond-request/request-status/{request-status}/user-name/{user-name}")
    public ResponseEntity<Void> respondRequest(@PathVariable("request-status") RequestStatus requestStatus, @PathVariable("user-name") String userName) {
        return careGiverCareRecipientService.respondRequest(requestStatus,userName);
    }

    @PostMapping("remove-recipient/user-name/{user-name}")
    public ResponseEntity<Void> removeRecipient(@PathVariable("user-name") String userName) {
        return careGiverCareRecipientService.removeRecipient(userName);
    }
    @PostMapping("remove-caregiver")
    public ResponseEntity<Void> removeCareGiver() {
        return careGiverCareRecipientService.removeCareGiver(UserContext.getUserId());
    }

    @PostMapping("get-token/user-name/{user-name}")
    public ResponseEntity<LoginResponse> getToken(@PathVariable("user-name") String userName,@RequestHeader("x-session-id") String sessionId) {
        return careGiverCareRecipientService.getToken(userName,sessionId);
    }
    @GetMapping("phone-number/linked/users/{phone-number}")
    public List<CareGiverCareRecipientResponse> phoneNumberLinkedUsers(@PathVariable("phone-number") String phoneNumber) {
        return careGiverCareRecipientService.phoneNumberLinkedUsers(phoneNumber);
    }

}