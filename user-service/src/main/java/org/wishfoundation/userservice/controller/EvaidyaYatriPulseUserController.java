package org.wishfoundation.userservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.response.EvaidyaUserResponse;
import org.wishfoundation.userservice.response.UserDetailsResponse;
import org.wishfoundation.userservice.service.EvaidyaYatriPulseUserServiceImpl;

/**
 * Controller for Evaidya Yatri Pulse User related operations.
 *
 */
@RestController
@RequestMapping("/api/v1/evaidya")
public class EvaidyaYatriPulseUserController {

    /**
     * Autowired instance of EvaidyaYatriPulseUserServiceImpl.
     */
    @Autowired
    private EvaidyaYatriPulseUserServiceImpl evaidyaYatriPulseUserService;

    /**
     * Get user details by username.
     *
     * @param username The username of the user.
     * @return UserDetailsResponse containing user details.
     */
    @GetMapping("/get-user/{user-name}")
    public UserDetailsResponse getYatriPulseUser(@PathVariable("user-name") String username) {
        return evaidyaYatriPulseUserService.getYatriDetails(username);
    }

    /**
     * Sign up a new user.
     *
     * @param yatriPulseUserRequest The request containing user details.
     * @return ResponseEntity containing EvaidyaUserResponse.
     */
    @PostMapping("/sign-up")
    public ResponseEntity<EvaidyaUserResponse> signUp(@Valid @RequestBody YatriPulseUserRequest yatriPulseUserRequest) {
        return evaidyaYatriPulseUserService.signUp(yatriPulseUserRequest);
    }

    /**
     * Update user details.
     *
     * @param yatriPulseUserRequest The request containing updated user details.
     * @return ResponseEntity with no content (status 204).
     */
    @PatchMapping("/update")
    public ResponseEntity<Void> updateUser(@Valid @RequestBody YatriPulseUserRequest yatriPulseUserRequest) {
        return evaidyaYatriPulseUserService.updateUser(yatriPulseUserRequest);
    }

}
