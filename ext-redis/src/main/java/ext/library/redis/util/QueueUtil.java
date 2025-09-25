package ext.library.redis.util;

import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式队列工具 轻量级队列 重量级数据量 请使用 MQ 要求 redis 5.X 以上
 */
public class QueueUtil {

    /**
     * 添加普通队列数据
     *
     * @param queueName 队列名
     * @param data      数据
     */
    public static void producer(String queueName, String data) {
        RedisUtil.listOps().leftPush(queueName, data);
    }

    /**
     * 通用获取一个队列数据 没有数据返回 null(不支持延迟队列)
     *
     * @param queueName 队列名
     */
    public static String consumer(String queueName) {
        return RedisUtil.listOps().rightPop(queueName);
    }

    /**
     * 通用删除队列数据 (不支持延迟队列)
     */
    public static void remove(String queueName, String data) {
        RedisUtil.listOps().remove(queueName, 0, data);
    }

    /**
     * 通用销毁队列 所有阻塞监听 报错 (不支持延迟队列)
     */
    public static void destroy(String queueName) {
        RedisUtil.getRedisTemplate().delete(queueName);
    }

    /**
     * 添加延迟队列数据 默认秒
     *
     * @param queueName 队列名
     * @param data      数据
     * @param time      延迟时间
     */
    public static void delayedProducer(String queueName, String data, long time) {
        long score = System.currentTimeMillis() / 1000 + time;
        RedisUtil.zSetOps().add(queueName, data, score);
    }

    /**
     * 添加延迟队列数据 默认秒
     *
     * @param queueName 队列名
     * @param data      数据
     * @param time      延迟时间
     */
    public static void delayedProducer(String queueName, String data, Duration time) {
        long score = time.getSeconds();
        RedisUtil.zSetOps().add(queueName, data, score);
    }

    /**
     * 添加延迟队列数据
     *
     * @param queueName 队列名
     * @param data      数据
     * @param time      延迟时间
     * @param timeUnit  单位
     */
    public static void delayedProducer(String queueName, String data, long time, TimeUnit timeUnit) {
        long seconds = timeUnit.toSeconds(time);
        delayedProducer(queueName, data, seconds);
    }

    /**
     * 获取一个延迟队列数据 没有数据返回 null
     *
     * @param queueName 队列名
     */
    public static String delayedConsumer(String queueName) {
        // language=redis
        String luaScript = """
                -- KEYS[1] 延时队列的 key
                -- ARGV[1] 当前时间戳
                -- 返回值：任务 ID（如果存在）或 nil
                
                local key = KEYS[1]
                local currentTime = tonumber(ARGV[1])
                
                -- 使用 zrangebyscore 和 zrem 的组合命令 zpopmin，它原子性地返回并移除分数最低的元素
                -- 这里假设 Redis 版本支持 zpopmin 命令（Redis 5.0 及以上版本）
                local task = redis.call('zpopmin', key, 1, 'BLOCK', 0, 'SCORES')
                
                -- zpopmin 返回的是一个包含两个元素的数组，第一个元素是分数，第二个是成员
                if task and #task > 0 and task[2] and tonumber(task[1]) <= currentTime then
                    return task[2] -- 返回任务 ID
                else
                    return nil
                end
                """; // 上面定义的 Lua 脚本内容
        RedisScript<String> script = RedisScript.of(luaScript, String.class);
        long currentTime = System.currentTimeMillis() / 1000;
        return RedisUtil.getRedisTemplate()
                .execute(script, Collections.singletonList(queueName), String.valueOf(currentTime));
    }

    /**
     * 删除延迟队列数据
     */
    public static void delayedRemove(String queueName, String data) {
        RedisUtil.zSetOps().remove(queueName, data);
    }

}