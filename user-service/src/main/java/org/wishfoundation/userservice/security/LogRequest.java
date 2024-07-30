package org.wishfoundation.userservice.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogRequest {
   private String clientIp;
    private String userAgent;
    private String dateTime;
    private String username;
    private String sessionId;
    private String referrer;
    private String url;
    private String requestId;

}
