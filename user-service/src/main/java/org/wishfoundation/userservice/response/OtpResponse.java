package org.wishfoundation.userservice.response;

import lombok.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponse {
    private String message;
    private Long attemptLeft;

    private boolean rateLimitExceed;
}
