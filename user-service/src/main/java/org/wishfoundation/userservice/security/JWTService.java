package org.wishfoundation.userservice.security;


import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.wishfoundation.userservice.utils.Helper;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTService {


    private final String SECRET_KEY = "X7s&=&m$&OCiS/<";
    public static final String USER_NAME = "USER_NAME";

    public static final String USER_ROLE = "USER_ROLE";

    public static final String PHONE_NUMBER = "PHONE_NUMBER";

    public static final String MASTER_TOKEN = "MASTER_TOKEN";

    public static final String SESSION_ID = "SESSION_ID";
    @Autowired
    private RedisTemplate redisTemplate;

    public String generateToken(Authentication authentication, String phoneNumber, String sessionId) {

        Map<String, Object> claims = new HashMap<>();
        String username = authentication.getName();


        try {
            Map<String, Object> details = Helper.MAPPER.convertValue(authentication.getDetails(), new TypeReference<Map<String, Object>>() {
            });
            claims.put(USER_ROLE, (List) details.get(USER_ROLE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        claims.put(USER_NAME, username);
        claims.put(PHONE_NUMBER, phoneNumber);
        //   claims.put(SESSION_ID, sessionId);
        String token = "";
        if (!redisTemplate.hasKey(username + sessionId)) {
            redisTemplate.opsForValue().set(username + sessionId, sessionId, Duration.ofHours(24));
        }
        if (username.equalsIgnoreCase("demouser03")) {
            token = Jwts.builder().setClaims(claims).setIssuer("YATRI_PULSE").setSubject(username)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis()
                            + (5L * 60 * 1000) - 10000))
                    .setNotBefore(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                    .compact();
        } else {
            token = Jwts.builder().setClaims(claims).setIssuer("YATRI_PULSE").setSubject(username)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis()
                            + (1000 * 60 * 60 * 24) - 10000))
                    .setNotBefore(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                    .compact();
        }
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

    public String generateMasterToken(String userName, String phoneNumber, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_NAME, userName);
        claims.put(PHONE_NUMBER, phoneNumber);
        claims.put(MASTER_TOKEN, "true");
        if (!redisTemplate.hasKey(userName + sessionId)) {
            redisTemplate.opsForValue().set(userName + sessionId, sessionId, Duration.ofHours(24));
        }
        return Jwts.builder().setClaims(claims).setIssuer("YATRI_PULSE").setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                        + (1000 * 60 * 60 * 24) - 10000))
                .setNotBefore(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

}

