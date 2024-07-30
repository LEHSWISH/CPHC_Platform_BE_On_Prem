package org.wishfoundation.iomtdeviceinventory.security;


import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.wishfoundation.iomtdeviceinventory.utils.Helper;

import java.util.*;

@Component
public class JWTService {


    private final String SECRET_KEY = "X7s&=&m$&OCiS/<";
    public static final String USER_NAME = "USER_NAME";

    public static final String SESSION_ID = "SESSION_ID";

    public static final String PHONE_NUMBER = "PHONE_NUMBER";


    public String generateToken(String userName, String phoneNumber) {

        Map<String, Object> claims = new HashMap<>();

        claims.put(USER_NAME, userName);
        claims.put(PHONE_NUMBER, phoneNumber);
        claims.put(SESSION_ID, UUID.randomUUID());

        String token = Jwts.builder().setClaims(claims).setIssuer("ESWASTHYA_DHAM").setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                        + (1000 * 60 * 60 * 24) - 10000))
                .setNotBefore(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        return token;
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getPhoneNumberFromJWT(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody().get(PHONE_NUMBER, String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect", ex.fillInStackTrace());
        }
    }

}

