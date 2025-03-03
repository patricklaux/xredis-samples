package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.lettuce.api.Pipeline;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pipeline 自动配置类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/3
 */
@Configuration
public class PipelineAutoConfiguration {

    /**
     * 通过注入的 RedisOperatorFactory 创建 Pipeline
     *
     * @param factory RedisOperatorFactory
     * @return Pipeline
     */
    @Bean
    Pipeline<String, String> pipeline(RedisOperatorFactory factory) {
        return factory.pipeline(StringCodec.UTF8);
    }

}
