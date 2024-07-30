package org.wishfoundation.iomtdeviceinventory.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Configuration
@EnableAsync(proxyTargetClass = true)
public class HeaderInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);



    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {

        String requestId = generateUniqueId();
        response.addHeader("x-request-id", requestId);
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous";

        logger.info("call initiated || user name: " + username + " || request id: " + requestId);

        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) throws Exception {

        SecurityContextHolder.clearContext();
        logger.info("call completed");
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
