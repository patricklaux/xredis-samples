package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.RedisFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/3
 */
@SpringBootTest
public class RedisOperatorTest {

    @Autowired
    private RedisOperator<String, String> redisOperator;

    @Test
    void hget() {
        String key = "test-key", field = "test-field", value = "test-value";
        redisOperator.sync().del(key);

        Boolean status = redisOperator.sync().hset(key, field, value);
        Assertions.assertEquals(Boolean.TRUE, status);

        String result = redisOperator.sync().hget(key, field);
        Assertions.assertEquals(value, result);
    }

    @Test
    void hgetAsync() throws ExecutionException, InterruptedException {
        String key = "test-key", field = "test-field", value = "test-value";
        redisOperator.sync().del(key);

        RedisFuture<Boolean> future = redisOperator.async().hset(key, field, value);
        Assertions.assertEquals(Boolean.TRUE, future.get());

        RedisFuture<String> result = redisOperator.async().hget(key, field);
        Assertions.assertEquals(value, result.get());
    }

    @Test
    void hgetReactive() {
        String key = "test-key", field = "test-field", value = "test-value";
        redisOperator.sync().del(key);

        Mono<Boolean> mono = redisOperator.reactive().hset(key, field, value);
        Assertions.assertEquals(Boolean.TRUE, mono.block());

        Mono<String> result = redisOperator.reactive().hget(key, field);
        Assertions.assertEquals(value, result.block());
    }

}
