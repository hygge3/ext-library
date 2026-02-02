package ext.library.postgres.util;

import ext.library.core.util.SpringUtil;
import ext.library.postgres.cache.PostgresCacheManager;
import ext.library.postgres.pubsub.PostgresPubSub;
import ext.library.postgres.queue.Job;
import ext.library.postgres.queue.PostgresQueue;
import ext.library.postgres.ratelimit.PostgresRateLimiter;
import ext.library.postgres.ratelimit.RateLimitResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * PostgreSQL 操作静态工具类
 * <p>
 * 提供缓存、队列、发布订阅、限流、会话等功能的静态方法访问
 *
 * @since 4.0.0
 */
public final class PostgresUtil {

    private PostgresUtil() {
    }

    // ==================== 缓存操作 ====================

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     *
     * @return 缓存值，不存在返回 null
     */
    public static String cacheGet(String key) {
        return getCacheManager().get(key);
    }

    /**
     * 获取缓存值并反序列化
     *
     * @param key   缓存键
     * @param clazz 目标类型
     *
     * @return 缓存值，不存在返回 null
     */
    public static <T> T cacheGet(String key, Class<T> clazz) {
        return getCacheManager().get(key, clazz);
    }

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public static void cacheSet(String key, Object value) {
        getCacheManager().set(key, value);
    }

    /**
     * 设置缓存值（指定过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     */
    public static void cacheSet(String key, Object value, Duration ttl) {
        getCacheManager().set(key, value, ttl);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     *
     * @return 是否删除成功
     */
    public static boolean cacheDelete(String key) {
        return getCacheManager().delete(key);
    }

    /**
     * 按模式删除缓存
     *
     * @param pattern 匹配模式（支持 * 通配符）
     *
     * @return 删除的数量
     */
    public static int cacheDeleteByPattern(String pattern) {
        return getCacheManager().deleteByPattern(pattern);
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     *
     * @return 是否存在
     */
    public static boolean cacheExists(String key) {
        return getCacheManager().exists(key);
    }

    /**
     * 设置缓存过期时间
     *
     * @param key 缓存键
     * @param ttl 过期时间
     *
     * @return 是否设置成功
     */
    public static boolean cacheExpire(String key, Duration ttl) {
        return getCacheManager().expire(key, ttl);
    }

    // ==================== 队列操作 ====================

    /**
     * 任务入队（立即执行）
     *
     * @param queue   队列名称
     * @param payload 任务负载
     *
     * @return 任务 ID
     */
    public static long enqueue(String queue, Object payload) {
        return getQueue().enqueue(queue, payload);
    }

    /**
     * 任务入队（延迟执行）
     *
     * @param queue   队列名称
     * @param payload 任务负载
     * @param delay   延迟时间
     *
     * @return 任务 ID
     */
    public static long enqueue(String queue, Object payload, Duration delay) {
        return getQueue().enqueue(queue, payload, delay);
    }

    /**
     * 任务入队（指定时间执行）
     *
     * @param queue       队列名称
     * @param payload     任务负载
     * @param scheduledAt 计划执行时间
     *
     * @return 任务 ID
     */
    public static long enqueue(String queue, Object payload, Instant scheduledAt) {
        return getQueue().enqueue(queue, payload, scheduledAt);
    }

    /**
     * 任务出队
     *
     * @param queue 队列名称
     *
     * @return 任务，不存在返回空
     */
    public static Optional<Job> dequeue(String queue) {
        return getQueue().dequeue(queue);
    }

    /**
     * 批量任务出队
     *
     * @param queue 队列名称
     * @param limit 最大获取数量
     *
     * @return 任务列表
     */
    public static List<Job> dequeue(String queue, int limit) {
        return getQueue().dequeue(queue, limit);
    }

    /**
     * 完成任务
     *
     * @param jobId 任务 ID
     */
    public static void completeJob(long jobId) {
        getQueue().complete(jobId);
    }

    /**
     * 任务失败
     *
     * @param jobId 任务 ID
     * @param error 错误信息
     */
    public static void failJob(long jobId, String error) {
        getQueue().fail(jobId, error);
    }

    /**
     * 任务失败
     *
     * @param jobId 任务 ID
     * @param e     异常
     */
    public static void failJob(long jobId, Throwable e) {
        getQueue().fail(jobId, e);
    }

    /**
     * 获取队列待处理任务数量
     *
     * @param queue 队列名称
     *
     * @return 待处理数量
     */
    public static long queuePendingCount(String queue) {
        return getQueue().countPending(queue);
    }

    /**
     * 获取队列统计信息
     *
     * @param queue 队列名称
     *
     * @return [pending, processing, completed, failed]
     */
    public static long[] queueStats(String queue) {
        return getQueue().getQueueStats(queue);
    }

    // ==================== 发布订阅操作 ====================

    /**
     * 发布消息
     *
     * @param channel 通道名称
     * @param message 消息内容
     */
    public static void publish(String channel, Object message) {
        getPubSub().publish(channel, message);
    }

    /**
     * 订阅通道
     *
     * @param channel  通道名称
     * @param callback 消息回调
     */
    public static void subscribe(String channel, Consumer<String> callback) {
        getPubSub().subscribe(channel, callback);
    }

    /**
     * 订阅通道（带类型反序列化）
     *
     * @param channel  通道名称
     * @param clazz    消息类型
     * @param callback 消息回调
     */
    public static <T> void subscribe(String channel, Class<T> clazz, Consumer<T> callback) {
        getPubSub().subscribe(channel, clazz, callback);
    }

    /**
     * 取消订阅
     *
     * @param channel 通道名称
     */
    public static void unsubscribe(String channel) {
        getPubSub().unsubscribe(channel);
    }

    /**
     * 检查是否已订阅
     *
     * @param channel 通道名称
     *
     * @return 是否已订阅
     */
    public static boolean isSubscribed(String channel) {
        return getPubSub().isSubscribed(channel);
    }

    // ==================== 限流操作 ====================

    /**
     * 检查并递增（使用默认配置）
     *
     * @param key 限流键
     *
     * @return 限流结果
     */
    public static RateLimitResult rateLimit(String key) {
        return getRateLimiter().checkAndIncrement(key);
    }

    /**
     * 检查并递增
     *
     * @param key    限流键
     * @param limit  限制阈值
     * @param window 时间窗口
     *
     * @return 限流结果
     */
    public static RateLimitResult rateLimit(String key, int limit, Duration window) {
        return getRateLimiter().checkAndIncrement(key, limit, window);
    }

    /**
     * 仅检查是否允许（不递增）
     *
     * @param key 限流键
     *
     * @return 是否允许
     */
    public static boolean isRateLimitAllowed(String key) {
        return getRateLimiter().isAllowed(key);
    }

    /**
     * 仅检查是否允许（不递增）
     *
     * @param key    限流键
     * @param limit  限制阈值
     * @param window 时间窗口
     *
     * @return 是否允许
     */
    public static boolean isRateLimitAllowed(String key, int limit, Duration window) {
        return getRateLimiter().isAllowed(key, limit, window);
    }

    /**
     * 重置限流计数
     *
     * @param key 限流键
     *
     * @return 是否重置成功
     */
    public static boolean resetRateLimit(String key) {
        return getRateLimiter().reset(key);
    }

    // ==================== Bean 获取 ====================

    /**
     * 获取缓存管理器 Bean
     */
    public static PostgresCacheManager getCacheManager() {
        return SpringUtil.getBean(PostgresCacheManager.class);
    }

    /**
     * 获取队列管理器 Bean
     */
    public static PostgresQueue getQueue() {
        return SpringUtil.getBean(PostgresQueue.class);
    }

    /**
     * 获取发布订阅管理器 Bean
     */
    public static PostgresPubSub getPubSub() {
        return SpringUtil.getBean(PostgresPubSub.class);
    }

    /**
     * 获取限流器 Bean
     */
    public static PostgresRateLimiter getRateLimiter() {
        return SpringUtil.getBean(PostgresRateLimiter.class);
    }
}
