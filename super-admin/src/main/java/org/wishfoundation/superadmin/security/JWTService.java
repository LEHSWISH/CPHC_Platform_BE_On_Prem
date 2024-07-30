package org.wishfoundation.superadmin.security;


import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.wishfoundation.superadmin.utils.Helper;


import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTService {


    private final String SECRET_KEY = "X7s&=&m$&OCiS/<";
    public static final String USER_EMAIL = "USER_EMAIL";

    public static final String USER_ROLE = "USER_ROLE";

    public static final String PHONE_NUMBER = "PHONE_NUMBER";

    public static final String MASTER_TOKEN = "MASTER_TOKEN";

    @Autowired
    private RedisTemplate redisTemplate;

    public String generateToken(Authentication authentication, String phoneNumber, String sessionId) {

        Map<String, Object> claims = new HashMap<>();
        String emailId = authentication.getName();

        try {
            Map<String, Object> details = Helper.MAPPER.convertValue(authentication.getDetails(), new TypeReference<Map<String, Object>>() {
            });
            claims.put(USER_ROLE, (List) details.get(USER_ROLE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        claims.put(USER_EMAIL, emailId);
        claims.put(PHONE_NUMBER, phoneNumber);
        String token = "";
        if (!redisTemplate.hasKey(emailId + sessionId)) {
            redisTemplate.opsForValue().set(emailId + sessionId, sessionId, Duration.ofHours(24));
        }

        token = Jwts.builder().setClaims(claims).setIssuer("YATRI_PULSE").setSubject(emailId)
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

