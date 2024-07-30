package org.wishfoundation.userservice.response;

import lombok.*;
import org.wishfoundation.userservice.enums.Gender;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TourismUserDetails {

    private String idtpId;
    private String fullName;
    private String phoneNumber;
    private Gender gender;
    private int age;
    private String attemptLeft;
    private String address;

}
