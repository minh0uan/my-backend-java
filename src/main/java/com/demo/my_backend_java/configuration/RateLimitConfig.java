package com.demo.my_backend_java.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;


@Configuration
public class RateLimitConfig {
    //fall back if the application.property is not working correctly
    @Value("${spring.data.redis.host:localhost}") 
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(
            RedisURI.builder()
                    .withHost(redisHost)
                    .withPort(redisPort)
                    .build()
        );
    }

    @Bean 
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        var redisConnection = redisClient.connect(
            RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );

        //ttl 
        var expirationStrategy =  ExpirationAfterWriteStrategy
            .basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1));

            
        var clientConfig = ClientSideConfig.getDefault()
            .withExpirationAfterWriteStrategy(expirationStrategy);

        return LettuceBasedProxyManager.builderFor(redisConnection)
            .withClientSideConfig(clientConfig)
            .build();
    }
}
