package org.wishfoundation.userservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.exception.WebClientCustomException;
import org.wishfoundation.userservice.exception.WishFoundationException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LogManager.getLogger(WebClientConfig.class);

    //100 MB change it as per requirement
    final int size = 100 * 1024 * 1024;
    final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size)).build();

    Set<String> staticErrorCodeHandling = new HashSet<>();

    @Profile({"itdastaging","prod"})
    @Bean
    public Set<String> itdaStagingErrorHandlingSet() {
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/api/abha/v3/profile/login/request/otp");
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/api/abha/v3/enrollment/enrol/abha-address");
        staticErrorCodeHandling.add("https://healthid.abdm.gov.in/api/v2/hid/benefit/createHealthId/demo/auth");
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/api/abha/v3/enrollment/enrol/byAadhaar");
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/api/abha/v3/profile/login/verify");
        staticErrorCodeHandling.add("https://healthid.abdm.gov.in/api/v2/auth/confirmWithAadhaarOtp");
        staticErrorCodeHandling.add("https://healthid.abdm.gov.in/api/v2/auth/confirmWithMobileOTP");
        return staticErrorCodeHandling;
    }

    @Profile({"dev","itdadev"})
    @Bean
    public Set<String> itdaDevErrorHandlingSet() {
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/abha/api/v3/profile/login/request/otp");
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/abha/api/v3/enrollment/enrol/abha-address");
        staticErrorCodeHandling.add("https://healthid.abdm.gov.in/api/v2/hid/benefit/createHealthId/demo/auth");
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/abha/api/v3/enrollment/enrol/byAadhaar");
        staticErrorCodeHandling.add("https://abha.abdm.gov.in/abha/api/v3/profile/login/verify");
        staticErrorCodeHandling.add("https://healthid.abdm.gov.in/api/v2/auth/confirmWithAadhaarOtp");
        staticErrorCodeHandling.add("https://healthid.abdm.gov.in/api/v2/auth/confirmWithMobileOTP");
        return staticErrorCodeHandling;
    }

    //ToDo: Handle the timeout too
    HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300000)
            .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(300))
                           .addHandlerLast(new WriteTimeoutHandler(300)));
    ConnectionProvider provider = ConnectionProvider.builder("fixed")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120)).build();

    //Logger for request
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
    //Todo: Handle exception handling too

    private ExchangeFilterFunction handleError() {

        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            if(staticErrorCodeHandling.contains(clientResponse.request().getURI().toString()) && (errorBody.contains("loginId") || errorBody.contains("ABDM-1115"))){
                                return Mono.error( new WishFoundationException(ErrorCode.MOBILE_NUMBER_NOT_IN_RECORD.getCode(),ErrorCode.MOBILE_NUMBER_NOT_IN_RECORD.getMessage()));
                            }
                            else if(errorBody.contains("loginId") || errorBody.contains("HIS-400") || errorBody.contains("ABDM-1116")){
                                return Mono.error(new WishFoundationException(ErrorCode.INVALID_AADHAAR_NUMBER.getCode(), ErrorCode.INVALID_AADHAAR_NUMBER.getMessage()));
                            }else if (staticErrorCodeHandling.contains(clientResponse.request().getURI().toString()) && (errorBody.contains("ABDM-1204") || errorBody.contains("Invalid X-token") ||errorBody.contains("HIS-422") || errorBody.contains("Invalid OTP value"))){
                                return Mono.error(new WishFoundationException(ErrorCode.INVALID_AADHAAR_OTP.getCode(), ErrorCode.INVALID_AADHAAR_OTP.getMessage()));
                            }
                            else if(errorBody.contains("ABDM-1114")){
                                return Mono.error( new WishFoundationException(ErrorCode.NO_AADHAAR_REGISTER.getCode(),ErrorCode.NO_AADHAAR_REGISTER.getMessage()));
                            }else if (staticErrorCodeHandling.contains(clientResponse.request().getURI().toString()) && errorBody.contains("ABDM-1101")){
                                return Mono.error(new WishFoundationException(ErrorCode.ABHA_ALREADY_EXIST.getCode(),ErrorCode.ABHA_ALREADY_EXIST.getMessage()));
                            }
                           else if(staticErrorCodeHandling.contains(clientResponse.request().getURI().toString()) && errorBody.contains("USR48")){
                                return Mono.error( new WishFoundationException(ErrorCode.NO_ABHA_REG_WITH_MOBILE.getCode(),ErrorCode.NO_ABHA_REG_WITH_MOBILE.getMessage()));
                            }
                             else if(clientResponse.request().getURI().toString().contains("/api/v2/auth/init")){
                                System.out.println("/api/v2/auth/init : "+errorBody);
                                return Mono.error( new WishFoundationException(ErrorCode.NO_ABHA_REG_WITH_ABHA_ADDRESS.getCode(),ErrorCode.NO_ABHA_REG_WITH_ABHA_ADDRESS.getMessage()));
                            }
//
                            return Mono.error(new WebClientCustomException(
                                clientResponse.statusCode().value(),
                                clientResponse.statusCode().toString(),
                                parseHeaders(clientResponse.headers().asHttpHeaders()),
                                errorBody));
                        });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

    private Map<String, String> parseHeaders(HttpHeaders headers) {
        Map<String, String> headerMap = new HashMap<>();
        headers.forEach((key, values) -> headerMap.put(key, values.get(0)));
        return headerMap;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider))).filter(logRequest()).filter(handleError()).exchangeStrategies(strategies);
    }

}
