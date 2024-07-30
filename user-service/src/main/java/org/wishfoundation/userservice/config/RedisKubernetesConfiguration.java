package org.wishfoundation.userservice.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
@Profile("kubernetes")
public class RedisKubernetesConfiguration {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;


    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }
    @Bean
    public RedissonClient redissonClientConfigDeployment(){
        Config config = new Config();
        config.useClusterServers().addNodeAddress("redis://"+redisHost+ ":" + redisPort)
                .setMasterConnectionPoolSize(1000)
                .setMasterConnectionMinimumIdleSize(5)
                .setIdleConnectionTimeout(300000) //5 min
                .setConnectTimeout(60000) // 1min
                .setTimeout(180000); //3 min // retry attempt is default 3 and retry attempt time : 1
        return Redisson.create(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedissonConnectionFactory redissonConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redissonConnectionFactory);
        // Configure serializers for keys and values
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Enable transaction support, if needed
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }


}
