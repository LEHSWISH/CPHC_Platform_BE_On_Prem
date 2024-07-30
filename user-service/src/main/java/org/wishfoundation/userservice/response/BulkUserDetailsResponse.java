package org.wishfoundation.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.wishfoundation.userservice.enums.Gender;

import java.util.UUID;

/**
 * This class represents the response structure for bulk user details.
 * It contains the necessary fields to provide user information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUserDetailsResponse {

    private String fullName;
    private String userName;
    private String phoneNumber;
    private String idtp;
    private Gender gender;
    private String address;
    private String state;
    private String district;

}
