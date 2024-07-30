package org.wishfoundation.superadmin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.wishfoundation.superadmin.config.UserAccountContext;
import org.wishfoundation.superadmin.exception.ExceptionDefinition;
import org.wishfoundation.superadmin.utils.Helper;

import java.io.IOException;
import java.util.Base64;


@Component
@Order(2)
public class JwtRequestFilter extends GenericFilterBean {

    public static final String AUTH_HEADER_VAL_PREFIX = "Bearer ";

    public static final String AUTH_HEADER_VAL_BASIC_PREFIX = "Basic ";
    public static final String AUTH_HEADER = "Authorization";
    public static final String SESSION_ID = "x-session-id";

    public static final String INTERNAL_REQUEST = "x-internal-request";

    @Autowired
    private JWTService jwtService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest wrapper) {
            if (servletResponse instanceof HttpServletResponse httpResponse) {

                String token = parseJwt(wrapper);

                if (!StringUtils.hasText(token)) {
                    filterChain.doFilter(servletRequest, servletResponse);
                    return;
                }

                if (StringUtils.hasText(token) && jwtService.validateToken(token)) {

                    String emailId = jwtService.getUsernameFromJWT(token);
                    String headerSessionId = wrapper.getHeader(SESSION_ID);

                    String internal = wrapper.getHeader(INTERNAL_REQUEST);
                    if (!StringUtils.hasLength(internal)) {
                        internal = "false";
                    }

                    if (!redisTemplate.hasKey(emailId + headerSessionId) && !internal.equalsIgnoreCase("true")) {
                        logger.warn("To access this insight, you must log in first.");
                        ExceptionDefinition def = new ExceptionDefinition();
                        def.setMessage("To access this insight, you must log in first.");
                        def.setExceptionClass(BadCredentialsException.class);
                        httpResponse.setContentType("application/json");
                        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                        httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                        UserAccountContext.clear();
                        return;
                    }

                    UserAccountContext.setCurrentToken(token);
                    UserAccountContext.setCurrentEmailId(emailId);
                    UserAccountContext.setCurrentPhoneNumber(jwtService.getPhoneNumberFromJWT(token));
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(emailId);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());
                    authenticationToken.setDetails(new AuthenticationDetails(wrapper));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    filterChain.doFilter(servletRequest, servletResponse);
                    UserAccountContext.clear();
                    return;
                }
            }
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTH_HEADER);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(AUTH_HEADER_VAL_PREFIX)) {
            return headerAuth.substring(AUTH_HEADER_VAL_PREFIX.length()).replace("\"", "");
        } else if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(AUTH_HEADER_VAL_BASIC_PREFIX)) {
            return new String(
                    Base64.getDecoder().decode(headerAuth.substring(AUTH_HEADER_VAL_BASIC_PREFIX.length())));
        }
        return null;
    }

}



