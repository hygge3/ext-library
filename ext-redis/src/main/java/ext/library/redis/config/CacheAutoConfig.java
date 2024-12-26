package ext.library.redis.config;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.library.redis.cache.aspect.CacheStringAspect;
import ext.library.redis.cache.manager.ExtRedisCacheManager;
import ext.library.redis.config.properties.RedisProperties;
import ext.library.redis.prefix.IRedisPrefixConverter;
import ext.library.redis.prefix.impl.DefaultRedisPrefixConverter;
import ext.library.redis.serialize.CacheSerializer;
import ext.library.redis.serialize.JacksonSerializer;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.lang.Nullable;

@AutoConfiguration(before = CacheAutoConfiguration.class)
@EnableConfigurationProperties(CacheProperties.class)
@RequiredArgsConstructor
public class CacheAutoConfig {

    private final ObjectMapper objectMapper;

    private final CacheProperties cacheProperties;

    private final RedisProperties redisProperties;

    @Nullable
    private final RedisCacheConfiguration redisCacheConfiguration;

    /**
     * 默认使用 Jackson 序列化
     *
     * @param objectMapper objectMapper
     * @return JacksonSerializer
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheSerializer cacheSerializer(ObjectMapper objectMapper) {
        return new JacksonSerializer(objectMapper);
    }

    /**
     * redis key 前缀处理器
     *
     * @return IRedisPrefixConverter
     */
    @Bean
    @DependsOn("cachePropertiesHolder")
    @ConditionalOnProperty(prefix = "ext.redis", name = "key-prefix")
    @ConditionalOnMissingBean(IRedisPrefixConverter.class)
    public IRedisPrefixConverter redisPrefixConverter() {
        return new DefaultRedisPrefixConverter(redisProperties.getKeyPrefix());
    }

    /**
     * 缓存注解操作切面</br>
     * 必须在 redisHelper 初始化之后使用
     *
     * @param stringRedisTemplate 字符串存储的 Redis 操作类
     * @param cacheSerializer     缓存序列化器
     * @return CacheStringAspect 缓存注解操作切面
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheStringAspect cacheStringAspect(StringRedisTemplate stringRedisTemplate,
                                               CacheSerializer cacheSerializer) {
        return new CacheStringAspect(stringRedisTemplate, cacheSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(CacheProperties.class)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = determineConfiguration();
        return new ExtRedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                defaultCacheConfig);
    }

    private RedisCacheConfiguration determineConfiguration() {
        if (this.redisCacheConfiguration != null) {
            return this.redisCacheConfiguration;
        } else {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    // 默认有效期 1 天
                    .entryTtl(Duration.ofDays(1))
                    // 设置默认缓存名分割符号为“:”，如果已经带“:”则不设置。
                    .computePrefixWith(name -> name.endsWith(Symbol.COLON) ? name : name + Symbol.COLON)
                    // 值使用 json 序列化
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Object.class)))
                    .disableCachingNullValues();
            if ($.isNull(cacheProperties)) {
                return config;
            }
            CacheProperties.Redis redisProperties = cacheProperties.getRedis();
            if (redisProperties.getTimeToLive() != null) {
                config = config.entryTtl(redisProperties.getTimeToLive());
            }
            if (redisProperties.getKeyPrefix() != null) {
                config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
            }
            if (!redisProperties.isCacheNullValues()) {
                config = config.disableCachingNullValues();
            }
            if (!redisProperties.isUseKeyPrefix()) {
                config = config.disableKeyPrefix();
            }
            return config;
        }
    }

}
