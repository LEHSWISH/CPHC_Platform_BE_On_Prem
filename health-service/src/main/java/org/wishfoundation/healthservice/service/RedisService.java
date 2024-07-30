package org.wishfoundation.healthservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.wishfoundation.healthservice.request.RedisServiceRequest;

import java.util.Objects;

/**
 * This class provides services related to Redis operations.
 * It uses Spring Data Redis for interacting with Redis.
 */
@RequiredArgsConstructor
@Service
public class RedisService {

    /**
     * The RedisTemplate instance provided by Spring Data Redis.
     */
    private final RedisTemplate redisTemplate;


    /**
     * Sets a value in Redis with the given key, value, timeout, and unit.
     *
     * @param redisServiceRequest The request object containing the necessary parameters.
     */
    public void setRedisValue(RedisServiceRequest redisServiceRequest){
         redisTemplate.opsForValue().set(redisServiceRequest.getKey(),redisServiceRequest.getValue(),redisServiceRequest.getTimeout(),redisServiceRequest.getUnit());
    }

    /**
     * Retrieves the value from Redis associated with the given key.
     *
     * @param redisServiceRequest The request object containing the necessary parameters.
     * @return The value associated with the given key, or an empty string if the key does not exist.
     */
    public String getRedisValue(RedisServiceRequest redisServiceRequest){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisServiceRequest.getKey()))) {
           return  Objects.requireNonNull(redisTemplate.opsForValue().get(redisServiceRequest.getKey())).toString();
        }
        return "";
    }

}
