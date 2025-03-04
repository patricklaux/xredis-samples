package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.lettuce.api.Pipeline;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.RedisFuture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

/**
 * Pipeline 测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/3
 */
@SpringBootTest
public class PipelineTest {

    @Resource
    private Pipeline<String, String> pipeline;

    @Resource
    private RedisOperator<String, String> redisOperator;

    @Test
    public void testBatch() throws ExecutionException, InterruptedException {
        // 1. 删除已有数据
        pipeline.del("key1", "key2", "key3");
        // 2. 设置数据
        pipeline.set("key1", "value1");
        pipeline.set("key2", "value2");
        pipeline.set("key3", "value3");
        // 3. 批量提交命令
        pipeline.flushCommands();

        // 4. 获取数据
        RedisFuture<String> result1 = pipeline.get("key1");
        RedisFuture<String> result2 = pipeline.get("key2");
        RedisFuture<String> result3 = pipeline.get("key3");
        // 5. 批量提交命令
        pipeline.flushCommands();

        Assertions.assertEquals("value1", result1.get());
        Assertions.assertEquals("value2", result2.get());
        Assertions.assertEquals("value3", result3.get());
    }


    @Test
    public void testBlock() throws InterruptedException {
        // 1. 同步删除和添加数据
        redisOperator.sync().del("string-key", "list-key");
        redisOperator.sync().set("string-key", "string-value");

        // 2. Pipeline 阻塞读取 Redis-List 数据
        pipeline.blpop(100000, "list-key")
                .thenAccept(result -> System.out.printf("blpop:\t %d\n", System.currentTimeMillis()));

        // 3. Pipeline 非阻塞读取 Redis-String 数据
        pipeline.get("string-key")
                .thenAccept(result -> System.out.printf("get:\t %d\n", System.currentTimeMillis()));

        // 4. 批量提交命令
        pipeline.flushCommands();

        System.out.printf("start:\t %d\n", System.currentTimeMillis());

        // 5. 休眠 5 秒后再向 Redis-List 添加数据
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                redisOperator.sync().rpush("list-key", "list-value1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.printf("end:\t %d\n", System.currentTimeMillis());

        Thread.sleep(10000);
    }

}
