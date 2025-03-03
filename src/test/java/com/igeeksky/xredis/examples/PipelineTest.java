package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.lettuce.api.Pipeline;
import io.lettuce.core.RedisFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/3
 */
@SpringBootTest
public class PipelineTest {

    @Autowired
    private Pipeline<String, String> pipeline;

    @Test
    public void test() throws ExecutionException, InterruptedException {
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

}
