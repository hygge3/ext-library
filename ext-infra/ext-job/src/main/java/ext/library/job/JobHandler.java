package ext.library.job;

import java.util.function.Consumer;

/**
 * 队列任务处理器
 * <p>
 * 每个队列注册一个处理器，队列中的负载以 JSON 字符串存储，出队后按 {@link #payloadType()}
 * 反序列化并回调。
 *
 * <pre>{@code
 * @Bean
 * JobHandler<Order> orderHandler(OrderService orderService) {
 *     return new JobHandler<>() {
 *         public Class<Order> payloadType() { return Order.class; }
 *         public void handle(Order order) { orderService.process(order); }
 *     };
 * }
 * }</pre>
 *
 * @param <T> 负载类型
 * @since 4.1.0
 */
public interface JobHandler<T> {

    /**
     * 默认队列名
     */
    String DEFAULT_QUEUE = JobDispatcher.DEFAULT_QUEUE;

    /**
     * 声明可处理的负载类型
     *
     * @return 负载类型
     */
    Class<T> payloadType();

    /**
     * 处理任务
     * <p>
     * 抛出异常即视为本次尝试失败，由队列按剩余重试次数决定重排或置为终态。
     *
     * @param payload 反序列化后的负载
     */
    void handle(T payload);

    /**
     * 队列名称
     * <p>
     * 未重写时使用 {@link #DEFAULT_QUEUE}。投递时队列必须已注册处理器，否则任务会堆积到重试耗尽。
     *
     * @return 队列名称
     */
    default String queue() {
        return DEFAULT_QUEUE;
    }

    /**
     * 用消费函数创建处理器
     *
     * @param payloadType 负载类型
     * @param consumer    处理逻辑
     * @param <T>         负载类型泛型
     *
     * @return 处理器
     */
    static <T> JobHandler<T> of(Class<T> payloadType, Consumer<T> consumer) {
        return of(DEFAULT_QUEUE, payloadType, consumer);
    }

    /**
     * 用消费函数创建指定队列的处理器
     *
     * @param queueName   队列名
     * @param payloadType 负载类型
     * @param consumer    处理逻辑
     * @param <T>         负载类型泛型
     *
     * @return 处理器
     */
    static <T> JobHandler<T> of(String queueName, Class<T> payloadType, Consumer<T> consumer) {
        return new JobHandler<>() {

            @Override
            public Class<T> payloadType() {
                return payloadType;
            }

            @Override
            public String queue() {
                return queueName;
            }

            @Override
            public void handle(T payload) {
                consumer.accept(payload);
            }

        };
    }

}
