package com.igeeksky.xredis.examples;

import com.igeeksky.xredis.common.StringTimeConvertor;
import com.igeeksky.xredis.common.flow.*;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.StreamPublisher;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Stream 测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/4
 */
@SpringBootTest
public class StreamTest {

    private static final Logger log = LoggerFactory.getLogger(StreamTest.class);

    private final UserStreamCodec streamCodec = new UserStreamCodec();

    @Resource
    private StreamOperator<String, String> streamOperator;

    @Resource
    private StreamContainer<String, String> streamContainer;

    @Resource
    private StreamPublisher<String, String, User> streamPublisher;

    @Resource
    private ScheduledExecutorService scheduler;

    @Test
    public void testSubscribe() throws InterruptedException {
        // 1. 获取 Redis Server 的当前时间戳作为读取消息的起始位置
        long serverTime = streamOperator.timeMillis(StringTimeConvertor.getInstance());

        // 2. 创建 XStreamOffset 对象（stream 名称 和 起始位置）
        XStreamOffset<String> offset = XStreamOffset.from("stream:user", serverTime + "-0");

        // 3. 订阅流，streamContainer 返回 Flow 对象（无限数据流）
        Flow<XStreamMessage<String, String>> flow = streamContainer.subscribe(offset);

        // 4. 创建订阅者，用以消费消息和处理异常
        Disposable disposable = flow.subscribe(new Subscriber<>() {

            @Override
            public void onNext(XStreamMessage<String, String> message) {
                Map<String, String> body = message.body();
                User user = streamCodec.decodeMsg(body);
                if (user.getId() % 1000 == 0) {
                    log.info("收到消息：{}", user);
                }
                if (user.getId() % 10000 == 0) {
                    throw new RuntimeException("模拟消费异常");
                }
            }

            /**
             * 消息拉取异常处理
             *
             * @param t   拉取消息引发的异常
             * @param s   订阅关系维护
             */
            @Override
            public void onError(Throwable t, Subscription s) {
                // 消息拉取异常，暂停拉取 100ms
                s.pausePull(Duration.ofMillis(100));
                log.error("消息拉取异常，暂停拉取 100ms...{}", t.getMessage(), t);
            }

            /**
             * 消息消费异常处理
             *
             * @param t         消费消息引发的异常
             * @param message   消息
             * @param attempts  消费失败次数
             * @param s         订阅关系维护
             */
            @Override
            public void onError(Throwable t, XStreamMessage<String, String> message, int attempts,
                                RetrySubscription<XStreamMessage<String, String>> s) {
                if (attempts < 4) {
                    // 允许失败 4 次，每次失败后重试间隔延长 100ms
                    log.warn("消费失败，重试中...{}", t.getMessage(), t);
                    s.retry(message, Duration.ofMillis(attempts * 100L));
                } else {
                    log.error("消费失败，超过最大重试次数，放弃重试...{}", t.getMessage(), t);
                }
            }
            // 并行度为 2，即最多会有 2 个线程同时调用 Subscriber
        }, 2);

        // 5. 使用额外线程开始发布消息
        Future<?> publishFuture = scheduler.submit(() -> {
            for (long i = 0; i < 500000; i++) {
                if (i % 5000 == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
                streamPublisher.publish(new User(i, "user-" + i, 18));
            }
        });

        // 6. 休眠 10 秒，观察期间的消息发布和消费信息打印
        Thread.sleep(10000);

        // 7. 取消订阅，但正在消费的当前数据仍将等待其完成
        disposable.dispose();

        // 8. 再次休眠 10 秒，取消订阅成功后消息拉取和消费都会停止，不会再有消息打印
        Thread.sleep(10000);

        // 9. 取消发布消息
        publishFuture.cancel(true);
    }

}
