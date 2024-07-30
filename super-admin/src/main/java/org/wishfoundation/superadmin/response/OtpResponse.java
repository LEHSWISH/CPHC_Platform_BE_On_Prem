package org.wishfoundation.superadmin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponse {
    private String message;
    private Long attemptLeft;

    private boolean rateLimitExceed;
}
