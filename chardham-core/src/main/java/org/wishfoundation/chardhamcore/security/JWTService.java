package org.wishfoundation.chardhamcore.security;


import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.wishfoundation.chardhamcore.utils.HelperCommon;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTService {

    private final String SECRET_KEY="X7s&=&m$&OCiS/<";
    public static  final String USER_NAME = "USER_NAME";

    public static  final String USER_ROLE = "USER_ROLE";

    public static final String PHONE_NUMBER= "PHONE_NUMBER";




    public String generateToken(Authentication authentication)  {

            Map<String, Object> claims = new HashMap<>();
            String username = authentication.getName();


            try {
                Map<String, Object> details = HelperCommon.MAPPER.convertValue(authentication.getDetails(), new TypeReference<Map<String, Object>>() {
                });
                claims.put(USER_ROLE, (List)details.get(USER_ROLE));
            }catch (Exception e){
                e.printStackTrace();
            }

            claims.put(USER_NAME, username);



            String token = Jwts.builder().setClaims(claims).setIssuer("YATRI_PULSE").setSubject(username)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis()
                            + (1000 * 60 * 60 * 7) - 10000))
                    .setNotBefore(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                    .compact();
            return token;
        }
        public String getUsernameFromJWT(String token){
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        }

    public String getPhoneNumberFromJWT(String token){
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody().get(PHONE_NUMBER,String.class);
    }


    public boolean validateToken(String token) {
            try {
                Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token);
                return true;
            } catch (Exception ex) {
                throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect",ex.fillInStackTrace());
            }
        }

    }

