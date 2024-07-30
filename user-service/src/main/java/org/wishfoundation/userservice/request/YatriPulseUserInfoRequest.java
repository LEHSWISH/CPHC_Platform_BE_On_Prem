package org.wishfoundation.userservice.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class YatriPulseUserInfoRequest {
    private String idYp;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]+\\s[a-zA-Z]+$", message = "User Name cannot contain special character")
    private String userName;

//    @NotEmpty
//    @Pattern(regexp = "^[a-zA-Z]+\\s[a-zA-Z]+$", message = "User First Name cannot contain special character")
//    private String userLastName;

    @Email(message = "Invalid email address. Please enter a proper email ID.")
    private String email;
    private String password;

    private String idTP;

    @Pattern(regexp="(^$|[0-9]{10})" , message = "Invalid phone number. Please enter a proper Phone Number")
    private String phoneNumber;

    private String aadhaarNumber;

    private String gender;

    private int age;

    @FutureOrPresent
    private LocalDate startTourDate;

    @FutureOrPresent
    private LocalDate endTourDate;

    @FutureOrPresent
    private LocalDate travelDate;

}
