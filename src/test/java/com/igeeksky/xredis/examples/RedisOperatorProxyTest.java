package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.KeyValue;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RedisOperatorProxy 测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/4
 */
@SpringBootTest
public class RedisOperatorProxyTest {

    @Resource
    RedisOperatorProxy redisProxy;

    /**
     * Redis-String：批量添加 100 万条数据，然后再批量删除
     */
    @Test
    public void mset_del() {
        int size = 1000000;
        byte[][] keys = new byte[size][];
        Map<byte[], byte[]> keysValues = HashMap.newHashMap(size);

        // 1. 创建 1000000 键值对作为测试数据
        for (int i = 0; i < size; i++) {
            byte[] key = ("test:del:key:" + i).getBytes();
            byte[] value = ("test:del:value:" + i).getBytes();
            keys[i] = key;
            keysValues.put(key, value);
        }

        // 2. 删除测试数据（同步）
        redisProxy.del(keys);

        // 3. 批量添加数据（同步）
        Assertions.assertEquals("OK", redisProxy.mset(keysValues));

        // 4. 批量删除数据（同步）
        Assertions.assertEquals(size, redisProxy.del(keys));
    }

    /**
     * Redis-Hash：写入 100 万条数据，并为每个字段设置 3600 秒的过期时间
     */
    @Test
    void hmpset() {
        int size = 1000000;
        byte[] key = "test:hmpset:key".getBytes();

        // 1. 删除测试数据
        redisProxy.del(key);

        // 2. 创建 1000000 键值对作为测试数据
        List<KeyValue<byte[], byte[]>> fieldsValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            byte[] field = ("field:" + i).getBytes();
            byte[] value = ("value:" + i).getBytes();
            fieldsValues.add(KeyValue.create(field, value));
        }

        // 3. 批量设置字段值，并设置字段的过期时间为 3600000 毫秒
        CompletableFuture<List<Long>> future = redisProxy.hmpsetAsync(key, 3600000, fieldsValues);

        // 4. 无限等待批量设置结果
        future.thenAccept(status -> {
            // 5. 验证是否设置成功
            for (int i = 0; i < size; i++) {
                Assertions.assertEquals(1L, status.get(i));
            }
        }).join();
    }

}
