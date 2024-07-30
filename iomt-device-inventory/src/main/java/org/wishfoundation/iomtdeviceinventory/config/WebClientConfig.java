package org.wishfoundation.iomtdeviceinventory.config;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.iomtdeviceinventory.exception.WebClientCustomException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LogManager.getLogger(WebClientConfig.class);

    //100 MB change it as per requirement
    final int size = 100 * 1024 * 1024;
    final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size)).build();

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
                        .flatMap(errorBody -> Mono.error(new WebClientCustomException(
                                clientResponse.statusCode().value(),
                                clientResponse.statusCode().toString(),
                                parseHeaders(clientResponse.headers().asHttpHeaders()),
                                errorBody
                        )));
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
