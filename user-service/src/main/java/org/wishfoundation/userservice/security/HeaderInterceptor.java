package org.wishfoundation.userservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.wishfoundation.userservice.config.UserContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * This class is a Spring Boot Interceptor that adds a unique request ID to each incoming request.
 * It also logs the start and end of each request, along with the user's name.
 * It uses Redis for storing request IDs and logs.
 */
@Component
@Configuration
@EnableAsync(proxyTargetClass = true)
public class HeaderInterceptor implements HandlerInterceptor {

    /**
     * Logger for logging messages.
     */
    private static final Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);

    /**
     * RedisTemplate for interacting with Redis.
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * This method is called before the actual handler method is executed.
     * It generates a unique request ID, adds it to the response headers, and logs the start of the request.
     *
     * @param request  The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param handler  The handler method to be executed.
     * @return true if the execution chain should proceed, false otherwise.
     * @throws Exception If an error occurs during the execution.
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {

        String requestId = generateUniqueId();
        response.addHeader("x-request-id", requestId);
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous";
//TODO : After discussion with Himank and Deshdeepak, commented until asked to uncomment.

//        String clientIp = request.getHeader("x-client-ip");
//        String userAgent = request.getHeader("x-user-agent");
//
//        Date date = new Date();
//        String dateTime = date.toString();
//        String referrer = request.getHeader("Referer");
//        String url = request.getRequestURL().toString();
//        LogRequest logRequest = new LogRequest();
//        logRequest.setUrl(url);
//        logRequest.setUsername(username);
//        logRequest.setReferrer(referrer);
//        logRequest.setSessionId(request.getSession().getId());
//        logRequest.setDateTime(dateTime);
//        logRequest.setClientIp(clientIp != null ? clientIp : request.getRemoteAddr());
//        logRequest.setUserAgent(userAgent != null ? userAgent : request.getHeader("user-agent"));
//        logRequest.setRequestId(requestId);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
//        String formattedDate = dateFormat.format(date);
//        asyncMethod(formattedDate, logRequest);

        logger.info("call initiated || user name: " + username + " || request id: " + requestId);

        return true;
    }

    //    @Async
//    public void asyncMethod(String formattedDate, LogRequest logRequest) {
//        ListOperations<String, Object> valueOps = redisTemplate.opsForList();
//        valueOps.rightPush(formattedDate, logRequest);
//    }

    /**
     * This method is called after the handler method is executed.
     * It clears the user context and security context, and logs the completion of the request.
     *
     * @param request  The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param handler  The handler method that was executed.
     * @param ex       The exception that occurred during the execution, or null if no exception occurred.
     * @throws Exception If an error occurs during the execution.
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) throws Exception {

        UserContext.clear();
        SecurityContextHolder.clearContext();
        logger.info("call completed: " + response.getHeader("x-request-id"));
    }

    /**
     * This method generates a unique ID using UUID.
     *
     * @return A unique ID as a string.
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}