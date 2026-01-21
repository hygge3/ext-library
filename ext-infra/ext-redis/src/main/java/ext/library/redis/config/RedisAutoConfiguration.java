package ext.library.redis.config;

import ext.library.redis.prefix.DefaultRedisPrefixConverter;
import ext.library.redis.prefix.RedisPrefixConverter;
import ext.library.redis.properties.RedisProperties;
import ext.library.redis.serialize.PrefixJdkRedisSerializer;
import ext.library.redis.serialize.PrefixStringRedisSerializer;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Redis 自动配置类
 */
@AutoConfiguration(before = DataRedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedisPrefixConverter.class)
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisPrefixConverter prefixConverter, RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new PrefixStringRedisSerializer(prefixConverter));
        Logs.info(EmojiSymbol.REDIS, "载入模块: Redis");
        return template;
    }

    @Bean
    @ConditionalOnBean(RedisProperties.class)
    @ConditionalOnMissingBean(RedisPrefixConverter.class)
    public RedisPrefixConverter redisPrefixConverter(RedisProperties redisProperties) {
        return new DefaultRedisPrefixConverter(redisProperties);
    }

    @Bean
    @ConditionalOnBean(RedisPrefixConverter.class)
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisPrefixConverter prefixConverter, JsonMapper jsonMapper, RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 设置  key 的序列化方式为 自定义 String Key 序列化
        template.setKeySerializer(new PrefixJdkRedisSerializer(prefixConverter));
        // 设置 hash key 的序列化方式为 自定义 String Key 序列化
        template.setHashKeySerializer(new PrefixJdkRedisSerializer(prefixConverter));
        GenericJacksonJsonRedisSerializer valueSerializer = new GenericJacksonJsonRedisSerializer(jsonMapper);
        // 设置 value 的序列化方式为 JSON
        template.setValueSerializer(valueSerializer);
        // 设置 hash value 的序列化方式为 JSON
        template.setHashValueSerializer(valueSerializer);
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer result = new RedisMessageListenerContainer();
        result.setConnectionFactory(redisConnectionFactory);
        return result;
    }

}
