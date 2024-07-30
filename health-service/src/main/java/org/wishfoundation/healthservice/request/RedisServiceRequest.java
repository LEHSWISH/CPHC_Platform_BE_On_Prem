package org.wishfoundation.healthservice.request;

import lombok.*;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RedisServiceRequest {
    private String key;
    private String value;
    private long timeout;
    private TimeUnit unit;
}
