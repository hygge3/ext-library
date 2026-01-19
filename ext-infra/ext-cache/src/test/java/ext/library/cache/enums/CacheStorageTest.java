package ext.library.cache.enums;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.cache.strategy.CaffeineStrategy;
import ext.library.cache.strategy.L2Strategy;
import ext.library.cache.strategy.RedisStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * CacheStorage 枚举测试
 */
@DisplayName("CacheStorage 枚举测试")
class CacheStorageTest {

    @Test
    @DisplayName("测试枚举值完整性")
    void testEnumValues() {
        CacheStorage[] values = CacheStorage.values();

        assertEquals(3, values.length, "应有 3 个枚举值");
    }

    @Test
    @DisplayName("测试枚举值")
    void testEnumConstants() {
        assertNotNull(CacheStorage.REDIS, "REDIS 枚举值应存在");
        assertNotNull(CacheStorage.CAFFEINE, "CAFFEINE 枚举值应存在");
        assertNotNull(CacheStorage.L2, "L2 枚举值应存在");
    }

    @Test
    @DisplayName("测试枚举名称")
    void testEnumNames() {
        assertEquals("REDIS", CacheStorage.REDIS.name());
        assertEquals("CAFFEINE", CacheStorage.CAFFEINE.name());
        assertEquals("L2", CacheStorage.L2.name());
    }

    @Test
    @DisplayName("测试获取策略")
    void testGetCacheStrategy() {
        CacheStrategy redisStrategy = CacheStorage.REDIS.getCacheStrategy();
        CacheStrategy caffeineStrategy = CacheStorage.CAFFEINE.getCacheStrategy();
        CacheStrategy l2Strategy = CacheStorage.L2.getCacheStrategy();

        assertNotNull(redisStrategy, "Redis 策略不应为 null");
        assertNotNull(caffeineStrategy, "Caffeine 策略不应为 null");
        assertNotNull(l2Strategy, "L2 策略不应为 null");
    }

    @Test
    @DisplayName("测试策略类型正确性")
    void testStrategyTypes() {
        CacheStrategy redisStrategy = CacheStorage.REDIS.getCacheStrategy();
        CacheStrategy caffeineStrategy = CacheStorage.CAFFEINE.getCacheStrategy();
        CacheStrategy l2Strategy = CacheStorage.L2.getCacheStrategy();

        // 验证策略类型
        assertEquals(RedisStrategy.class, redisStrategy.getClass(), "应为 RedisStrategy");
        assertEquals(CaffeineStrategy.class, caffeineStrategy.getClass(), "应为 CaffeineStrategy");
        assertEquals(L2Strategy.class, l2Strategy.getClass(), "应为 L2Strategy");
    }

    @Test
    @DisplayName("测试策略实例一致性")
    void testStrategyInstanceConsistency() {
        CacheStrategy strategy1 = CacheStorage.REDIS.getCacheStrategy();
        CacheStrategy strategy2 = CacheStorage.REDIS.getCacheStrategy();

        // 同一枚举值的策略实例应相同
        assertSame(strategy1, strategy2, "策略实例应相同");
    }

    @Test
    @DisplayName("测试 valueOf 方法")
    void testValueOf() {
        assertEquals(CacheStorage.REDIS, CacheStorage.valueOf("REDIS"));
        assertEquals(CacheStorage.CAFFEINE, CacheStorage.valueOf("CAFFEINE"));
        assertEquals(CacheStorage.L2, CacheStorage.valueOf("L2"));
    }

    @Test
    @DisplayName("测试枚举顺序")
    void testEnumOrder() {
        CacheStorage[] values = CacheStorage.values();

        assertEquals(CacheStorage.REDIS, values[0], "第一个应为 REDIS");
        assertEquals(CacheStorage.CAFFEINE, values[1], "第二个应为 CAFFEINE");
        assertEquals(CacheStorage.L2, values[2], "第三个应为 L2");
    }
}
