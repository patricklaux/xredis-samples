package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.StreamPublisher;
import com.igeeksky.xredis.common.stream.XAddOptions;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.common.stream.container.StreamGenericContainer;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import com.igeeksky.xredis.lettuce.api.Pipeline;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.autoconfigure.LettuceAutoConfiguration;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 扩展自动配置类
 * <p>
 * {@link RedisOperatorFactory} 已通过 {@link LettuceAutoConfiguration} 在应用启动时预创建，
 * 这里直接使用即可，无需再创建。
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/3
 */
@Configuration
public class RedisExtendAutoConfiguration {

    /**
     * 创建 Pipeline
     *
     * @param factory 预创建的 RedisOperatorFactory
     * @return Pipeline
     */
    @Bean
    Pipeline<String, String> pipeline(RedisOperatorFactory factory) {
        return factory.pipeline(StringCodec.UTF8);
    }

    /**
     * 创建 RedisOperatorProxy
     *
     * @param factory 预创建的 RedisOperatorFactory
     * @return RedisOperatorProxy
     */
    @Bean
    RedisOperatorProxy redisProxy(RedisOperatorFactory factory) {
        RedisOperator<byte[], byte[]> redisOperator = factory.redisOperator(ByteArrayCodec.INSTANCE);
        // 单批次命令提交数量阈值
        int batchSize = 10000;
        // 同步阻塞超时时长
        long timeout = 60000;
        return new LettuceOperatorProxy(batchSize, timeout, redisOperator);
    }

    /**
     * 创建 StreamOperator
     *
     * @param factory 预创建的 RedisOperatorFactory
     * @return StreamOperator
     */
    @Bean
    StreamOperator<String, String> streamOperator(RedisOperatorFactory factory) {
        return factory.streamOperator(StringCodec.UTF8);
    }

    /**
     * 创建 StreamPublisher
     * <p>
     * 用于发布用户信息
     *
     * @param streamOperator 预创建的 StreamOperator
     * @return StreamPublisher
     */
    @Bean
    StreamPublisher<String, String, User> streamPublisher(StreamOperator<String, String> streamOperator) {
        XAddOptions options = XAddOptions.builder().maxLen(10000).approximateTrimming().build();
        return new StreamPublisher<>("stream:user", options, streamOperator, new UserStreamCodec());
    }

    /**
     * 创建 ScheduledExecutorService
     * <p>
     * 用于 StreamContainer 和 StreamGenericContainer 的定时任务
     *
     * @return ScheduledExecutorService
     */
    @Bean(destroyMethod = "shutdown")
    ScheduledExecutorService scheduler() {
        return Executors.newScheduledThreadPool(2);
    }

    /**
     * 创建 StreamContainer
     *
     * @param factory   预创建的 RedisOperatorFactory
     * @param scheduler 预创建的 ScheduledExecutorService
     * @return StreamContainer
     */
    @Bean(destroyMethod = "shutdown")
    StreamContainer<String, String> streamContainer(RedisOperatorFactory factory,
                                                    ScheduledExecutorService scheduler) {
        // 任务执行间隔，单位毫秒
        int taskInterval = 10;
        // 流读取参数：读阻塞时长 10 毫秒，一次任务每个 Stream 的最大读取数量为 1000 条
        ReadOptions readOptions = ReadOptions.from(10L, 1000);
        return factory.streamContainer(StringCodec.UTF8, scheduler, taskInterval, readOptions);
    }

    /**
     * 创建 StreamGenericContainer
     *
     * @param factory   预创建的 RedisOperatorFactory
     * @param scheduler 预创建的 ScheduledExecutorService
     * @return StreamGenericContainer
     */
    @Bean(destroyMethod = "shutdown")
    StreamGenericContainer<String, String> streamGenericContainer(RedisOperatorFactory factory,
                                                                  ScheduledExecutorService scheduler) {
        // 任务执行间隔，单位毫秒
        int taskInterval = 10;
        return factory.streamGenericContainer(StringCodec.UTF8, scheduler, taskInterval);
    }

}