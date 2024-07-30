package org.wishfoundation.userservice.security;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class AuthenticationDetails implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5700684154337094276L;

    private final String remoteAddress;
    private final String authHeader;

    public AuthenticationDetails(HttpServletRequest request) {
        this.remoteAddress = request.getRemoteAddr();
        this.authHeader = request.getHeader(JwtRequestFilter.AUTH_HEADER);
    }

    public final String getAuthHeader() {
        return authHeader;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

}
