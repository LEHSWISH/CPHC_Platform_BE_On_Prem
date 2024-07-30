package org.wishfoundation.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    public static final String BEAN_ID_FOR_ASYNC = "threadPoolAppWideTaskExecutor";

    @Override
    @Bean(name = BEAN_ID_FOR_ASYNC)
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(500);
        executor.setMaxPoolSize(500);
        executor.setQueueCapacity(30000);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(runnable -> new DelegatingSecurityContextRunnable(runnable));
        executor.initialize();
        return executor;
    }

    @Async
    public <T, R> CompletableFuture<R> execute(Function<T, R> function, T requestInput) {
        return CompletableFuture.completedFuture(function.apply(requestInput));
    }

}