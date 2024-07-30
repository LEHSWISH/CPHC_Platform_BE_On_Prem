package org.wishfoundation.userservice.response.abha;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
public class AbhaRegistrationResponse {
    String txnId;
    String mobileNumber;

    boolean mobileLinked;

    String transactionId;

}
