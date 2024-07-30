package org.wishfoundation.userservice.security;

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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.wishfoundation.userservice.config.ApplicationProperties;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.exception.ExceptionDefinition;
import org.wishfoundation.userservice.request.BasicAuthProperties;
import org.wishfoundation.userservice.utils.EnvironmentConfig;
import org.wishfoundation.userservice.utils.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
    private ApplicationProperties applicationProperties;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest instanceof jakarta.servlet.http.HttpServletRequest wrapper) {
            if (servletResponse instanceof HttpServletResponse httpResponse) {
                try {
                    String token = parseJwt(wrapper);

                    if (!StringUtils.hasText(token)) {
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }

                    if (wrapper.getHeader(AUTH_HEADER).startsWith(AUTH_HEADER_VAL_BASIC_PREFIX)) {
                        if (StringUtils.hasLength(wrapper.getHeader("X-Organization-Id"))) {
                            String organizationId = wrapper.getHeader("X-Organization-Id");
                            BasicAuthProperties basicAuthProperties = applicationProperties.getBasicAuth().getOrganizations().get(organizationId);

                            if (basicAuthProperties == null) {
                                ExceptionDefinition def = new ExceptionDefinition();
                                def.setMessage("Invalid Organization.");
                                def.setExceptionClass(BadCredentialsException.class);

                                httpResponse.setContentType("application/json");
                                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                                httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                                return;
                            }

                            String username = token.split(":")[0];
                            String password = token.split(":")[1];

                            if (basicAuthProperties.getUsername().equals(Helper.decrypt(token.split(":")[0])) && basicAuthProperties.getPassword().equals(Helper.decrypt(token.split(":")[1]))) {
                                if (organizationId.equals("wish")) {
                                    boolean flag = false;

                                    for (String api : EnvironmentConfig.IGNORED_API_FOR_AUTH) {
                                        String a = api.contains("*") ? api.split("\\*")[0] : api;
                                        if (a.equals("/"))
                                            continue;
                                        flag = wrapper.getRequestURL().toString().contains(a);
                                        if (flag)
                                            break;
                                    }

                                    if (!flag) {
                                        ExceptionDefinition def = new ExceptionDefinition();
                                        def.setMessage("Authorization is required to access this insight.");
                                        def.setExceptionClass(BadCredentialsException.class);

                                        httpResponse.setContentType("application/json");
                                        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                                        httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                                        return;
                                    }
                                }

                                if (organizationId.equals("evaidya")) {
                                    boolean flag;
                                    flag = wrapper.getRequestURL().toString().contains("/api/v1/evaidya");
                                    if (!flag) {
                                        ExceptionDefinition def = new ExceptionDefinition();
                                        def.setMessage("Authorization is required to access this insight.");
                                        def.setExceptionClass(BadCredentialsException.class);

                                        httpResponse.setContentType("application/json");
                                        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                                        httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                                        return;
                                    }
                                }

                                List<GrantedAuthority> authorities = new ArrayList<>();
                                authorities.add(new SimpleGrantedAuthority("ADMIN"));
                                UserContext.setCurrentUserName(Helper.decrypt(token.split(":")[0]));
                                UserContext.setCurrentOrganization(organizationId);
                                UserContext.setCurrentPhoneNumber("9999888888");
                                UserContext.setCurrentToken(token);
                                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password, authorities);
                                usernamePasswordAuthenticationToken.setDetails(new AuthenticationDetails(wrapper));
                                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                                filterChain.doFilter(servletRequest, servletResponse);
                                UserContext.clear();
                                return;
                            } else {
                                ExceptionDefinition def = new ExceptionDefinition();
                                def.setMessage("Invalid credentials.");
                                def.setExceptionClass(BadCredentialsException.class);

                                httpResponse.setContentType("application/json");
                                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                                httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                                return;
                            }
                        } else {
                            ExceptionDefinition def = new ExceptionDefinition();
                            def.setMessage("Invalid header.");
                            def.setExceptionClass(BadCredentialsException.class);

                            httpResponse.setContentType("application/json");
                            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                            httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                            return;
                        }
                    } else if (StringUtils.hasText(token) && jwtService.validateToken(token)) {

                        String username = jwtService.getUsernameFromJWT(token);
                        String headerSessionId = wrapper.getHeader(SESSION_ID);
                        String internal = wrapper.getHeader(INTERNAL_REQUEST);
                        if (!StringUtils.hasLength(internal)) {
                            internal = "false";
                        }

                        if (!redisTemplate.hasKey(username + headerSessionId) && !internal.equalsIgnoreCase("true")) {
                            logger.warn("To access this insight, you must log in first.");
                            ExceptionDefinition def = new ExceptionDefinition();
                            def.setMessage("To access this insight, you must log in first.");
                            def.setExceptionClass(BadCredentialsException.class);
                            httpResponse.setContentType("application/json");
                            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                            httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                            UserContext.clear();
                            return;
                        }

                        UserContext.setCurrentToken(token);
                        UserContext.setCurrentUserName(username);
                        UserContext.setCurrentOrganization("wish");
                        UserContext.setCurrentPhoneNumber(jwtService.getPhoneNumberFromJWT(token));
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                        authenticationToken.setDetails(new AuthenticationDetails(wrapper));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                        filterChain.doFilter(servletRequest, servletResponse);
                        UserContext.clear();
                        return;
                    }


                } catch (AuthenticationCredentialsNotFoundException e) {
                    logger.warn("JWT was expired or incorrect");
                    ExceptionDefinition def = new ExceptionDefinition();
                    def.setMessage("JWT was expired or incorrect");
                    def.setExceptionClass(AuthenticationCredentialsNotFoundException.class);
                    httpResponse.setContentType("application/json");
                    httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                    httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                    UserContext.clear();
                    return;
                } catch (Exception e) {
                    UserContext.clear();
                    e.printStackTrace();
                }

                logger.warn("JWT Token does not begin with Bearer String");
                ExceptionDefinition def = new ExceptionDefinition();
                def.setMessage("Token does not begin with Bearer");
                def.setExceptionClass(BadCredentialsException.class);

                httpResponse.setContentType("application/json");
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.getOutputStream().print(Helper.MAPPER.writeValueAsString(def));
                UserContext.clear();
                return;
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



