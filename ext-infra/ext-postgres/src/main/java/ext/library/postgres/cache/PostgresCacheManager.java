package ext.library.postgres.cache;

import ext.library.core.token.TokenBucketResult;
import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * PostgreSQL 缓存管理器
 * <p>
 * 使用 UNLOGGED 表实现高性能缓存，支持 TTL 过期和自动清理
 *
 * @since 4.0.0
 */
public class PostgresCacheManager {

    private final JdbcClient jdbcClient;
    private final PostgresProperties properties;
    private final String tableName;
    private final String bucketTableName;

    public PostgresCacheManager(JdbcClient jdbcClient, PostgresProperties properties) {
        this.jdbcClient = jdbcClient;
        this.properties = properties;
        this.tableName = properties.getCache().getTableName();
        this.bucketTableName = properties.getBucket().getTableName();
    }

    /**
     * 获取缓存值
     *
     * @param key   缓存 key
     * @param clazz 值类型
     *
     * @return 缓存值，不存在或已过期返回 null
     */
    public <T> T get(String key, Type clazz) {
        return jdbcClient.sql("SELECT value FROM " + tableName + " WHERE key = ? AND expires_at > NOW()").param(key).query(String.class).optional().map(json -> JsonUtil.<T>readObj(json, clazz)).orElse(null);
    }

    /**
     * 获取缓存值（字符串）
     *
     * @param key 缓存 key
     *
     * @return 缓存值，不存在或已过期返回 null
     */
    public String get(String key) {
        return get(key, String.class);
    }

    /**
     * 设置缓存值 (Upsert)
     *
     * @param key   缓存 key
     * @param value 缓存值
     * @param ttl   过期时间
     */
    public <T> void set(String key, T value, Duration ttl) {
        jdbcClient.sql("""
                INSERT INTO %s (key, value, expires_at)
                VALUES (?, ?::jsonb, NOW() + ?::interval)
                ON CONFLICT (key) DO UPDATE
                SET value = EXCLUDED.value, expires_at = EXCLUDED.expires_at, updated_at = NOW()
                """.formatted(tableName)).param(key).param(JsonUtil.toJson(value)).param(ttl.toSeconds() + " seconds").update();
    }

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param key   缓存 key
     * @param value 缓存值
     */
    public <T> void set(String key, T value) {
        set(key, value, properties.getCache().getDefaultExpireTime());
    }

    /**
     * 设置缓存值
     *
     * @param key        缓存 key
     * @param value      缓存值
     * @param ttlSeconds 过期时间（秒）
     */
    public <T> void set(String key, T value, long ttlSeconds) {
        set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 设置 value for key，同时为其设置过期时间（Redis SETEX 语义）
     *
     * @param key     缓存 key
     * @param value   字符串值
     * @param timeout 过期时间
     */
    public void setEx(String key, String value, Duration timeout) {
        jdbcClient.sql("""
                INSERT INTO %s (key, value, expires_at)
                VALUES (?, ?::jsonb, NOW() + ?::interval)
                ON CONFLICT (key) DO UPDATE
                SET value = EXCLUDED.value, expires_at = EXCLUDED.expires_at, updated_at = NOW()
                """.formatted(tableName)).param(key).param(JsonUtil.toJson(value)).param(timeout.toSeconds() + " seconds").update();
    }

    /**
     * 设置 value for key，同时为其设置过期时间（Redis SETEX 语义）
     *
     * @param key        缓存 key
     * @param value      字符串值
     * @param ttlSeconds 过期时间（秒）
     */
    public void setEx(String key, String value, long ttlSeconds) {
        setEx(key, value, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 当 key 不存在时设置 value（Redis SETNX 语义）
     *
     * @param key   缓存 key
     * @param value 字符串值
     *
     * @return true: 设置成功（key 不存在） false: 设置失败（key 已存在且未过期）
     */
    public boolean setNx(String key, String value) {
        return setNxEx(key, value, properties.getCache().getDefaultExpireTime());
    }

    /**
     * 当 key 不存在时设置 value 并指定过期时间（Redis SETNX + EXPIRE 语义）
     *
     * @param key     缓存 key
     * @param value   字符串值
     * @param timeout 过期时间
     *
     * @return true: 设置成功（key 不存在）false: 设置失败（key 已存在且未过期）
     */
    public boolean setNxEx(String key, String value, Duration timeout) {
        int rows = jdbcClient.sql("""
                WITH cleanup AS (
                    DELETE FROM %s WHERE key = ? AND expires_at < NOW()
                )
                INSERT INTO %s (key, value, expires_at)
                VALUES (?, ?::jsonb, NOW() + ?::interval)
                ON CONFLICT (key) DO NOTHING
                """.formatted(tableName, tableName)).param(key).param(key).param(JsonUtil.toJson(value)).param(timeout.toSeconds() + " seconds").update();
        return rows > 0;
    }

    /**
     * 当 key 不存在时设置 value 并指定过期时间（Redis SETNX + EXPIRE 语义）
     *
     * @param key        缓存 key
     * @param value      字符串值
     * @param ttlSeconds 过期时间（秒）
     *
     * @return true: 设置成功（key 不存在） false: 设置失败（key 已存在且未过期）
     */
    public boolean setNxEx(String key, String value, long ttlSeconds) {
        return setNxEx(key, value, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 计数器累加（固定窗口）
     * <p>
     * 窗口从第一次累加开始算，后续累加只加计数、不动 {@code expires_at}；过期后下一次累加重新开窗。
     * 每次都续期等于把窗口无限往后推，持续有流量时计数永不归零。
     *
     * @param key    缓存 key
     * @param delta  步长
     * @param window 窗口大小
     *
     * @return 累加后的计数
     */
    public long increment(String key, long delta, Duration window) {
        String interval = window.toSeconds() + " seconds";
        Long count = jdbcClient.sql("""
                        INSERT INTO %s (key, value, expires_at)
                        VALUES (?, to_jsonb(?::bigint), NOW() + ?::interval)
                        ON CONFLICT (key) DO UPDATE SET
                            value = CASE WHEN %s.expires_at <= NOW() THEN to_jsonb(?::bigint)
                                         ELSE to_jsonb((%s.value #>> '{}')::bigint + ?::bigint) END,
                            expires_at = CASE WHEN %s.expires_at <= NOW() THEN NOW() + ?::interval
                                              ELSE %s.expires_at END
                        RETURNING (value #>> '{}')::bigint
                        """.formatted(tableName, tableName, tableName, tableName, tableName))
                .param(key).param(delta).param(interval)
                .param(delta).param(delta)
                .param(interval)
                .query(Long.class)
                .single();
        return count == null ? 0L : count;
    }

    /**
     * 令牌桶取令牌（GCRA 等价实现）
     * <p>
     * 两条语句各自原子，不需要事务：先幂等建行（等于满桶），再把「理论到达时间」往前推一格，
     * 超突发容忍就影响 0 行。保留旧 tat 只在前推成功时发生，所以不会被拒绝的请求拖后。
     *
     * @param key          桶 key
     * @param permits      本次需要的令牌数
     * @param capacity     桶容量（允许的瞬时突发量）
     * @param refillPeriod 从空桶补满所需时长
     *
     * @return 取令牌结果：是否成、剩余令牌、被拒时的等待时间
     */
    public TokenBucketResult tryAcquireToken(String key, long permits, long capacity, Duration refillPeriod) {
        // 微秒：毫秒精度会把 >1000/s 的配额截断成 1ms/个，实际速率被悄悄压到 1000/s
        long interval = Math.max(1, refillPeriod.toNanos() / 1_000 / capacity);
        // 一条语句完成「建桶 / 条件扣减」：放行时返回扣减后的领先微秒数，被拒时没有返回行
        Long aheadMicros = jdbcClient.sql("""
                        INSERT INTO %1$s (key, tat) VALUES (?, NOW())
                        ON CONFLICT (key) DO UPDATE
                            SET tat = GREATEST(%1$s.tat, NOW()) + (? * ?::float8) * INTERVAL '1 microsecond'
                            WHERE GREATEST(%1$s.tat, NOW()) - NOW() <= ? * ?::float8 * INTERVAL '1 microsecond'
                        RETURNING (EXTRACT(EPOCH FROM (tat - NOW())) * 1000000)::bigint
                        """.formatted(bucketTableName))
                .params(key, interval, permits, interval, capacity - permits)
                .query(Long.class)
                .optional()
                .orElse(null);
        if (aheadMicros != null) {
            return TokenBucketResult.of(true, Math.max(0, aheadMicros) * 1_000L, interval * 1_000L, permits, capacity);
        }
        // 被拒：读出当前 tat 算要等多久，放行路径就不多跑这一条
        long currentMicros = jdbcClient.sql("""
                        SELECT GREATEST(0, EXTRACT(EPOCH FROM (GREATEST(tat, NOW()) - NOW())) * 1000000)::bigint
                        FROM %s WHERE key = ?
                        """.formatted(bucketTableName))
                .param(key)
                .query(Long.class)
                .optional()
                .orElse(0L);
        return TokenBucketResult.of(false, currentMicros * 1_000L, interval * 1_000L, permits, capacity);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存 key
     *
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        int rows = jdbcClient.sql("DELETE FROM " + tableName + " WHERE key = ?").param(key).update();
        return rows > 0;
    }

    /**
     * 按模式删除缓存
     *
     * @param pattern 匹配模式（支持 * 通配符）
     *
     * @return 删除的记录数
     */
    public int deleteByPattern(String pattern) {
        // 将 * 通配符转换为 SQL 的 %
        return jdbcClient.sql("DELETE FROM " + tableName + " WHERE key LIKE ?").param(pattern.replace("*", "%")).update();
    }

    /**
     * 检查缓存是否存在且未过期
     *
     * @param key 缓存 key
     *
     * @return 是否存在
     */
    public boolean exists(String key) {
        return jdbcClient.sql("SELECT 1 FROM " + tableName + " WHERE key = ? AND expires_at > NOW()").param(key).query(Integer.class).optional().isPresent();
    }

    /**
     * 获取缓存的剩余 TTL（秒）
     *
     * @param key 缓存 key
     *
     * @return 剩余秒数，不存在返回 -2，永不过期返回 -1
     */
    public long ttl(String key) {
        return jdbcClient.sql("SELECT EXTRACT(EPOCH FROM (expires_at - NOW()))::bigint AS ttl FROM " + tableName + " WHERE key = ?").param(key).query(Long.class).optional().map(ttl -> Math.max(ttl, 0)).orElse(-2L);
    }

    /**
     * 更新缓存的过期时间
     *
     * @param key 缓存 key
     * @param ttl 新的过期时间
     *
     * @return 是否更新成功
     */
    public boolean expire(String key, Duration ttl) {
        int rows = jdbcClient.sql("UPDATE " + tableName + " SET expires_at = NOW() + ?::interval WHERE key = ?").param(ttl.toSeconds() + " seconds").param(key).update();
        return rows > 0;
    }

    /**
     * 定时清理过期缓存
     */
    @Scheduled(fixedDelayString = "#{@postgresProperties.cache.cleanupInterval.toMillis()}")
    public void cleanup() {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE expires_at < NOW()").update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理过期缓存：{} 条", deleted);
        }
        // 桶不落 expires_at：tat 落后一小时说明这一小时没人用过，删掉等下次重建
        int buckets = jdbcClient.sql("DELETE FROM " + bucketTableName + " WHERE tat < NOW() - INTERVAL '1 hour'").update();
        if (buckets > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理空闲令牌桶：{} 条", buckets);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存记录总数
     */
    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE expires_at > NOW()").query(Long.class).single();
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        jdbcClient.sql("TRUNCATE TABLE %s".formatted(tableName)).update();
        Logs.info(EmojiSymbol.POSTGRES, "已清空所有缓存");
    }
}
