package ext.library.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.library.redis.config.properties.RedisProperties;
import ext.library.redis.prefix.IRedisPrefixConverter;
import ext.library.redis.serialize.PrefixJdkRedisSerializer;
import ext.library.redis.serialize.PrefixStringRedisSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

/**
 * Redis 自动配置类
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfig {

    final RedisConnectionFactory redisConnectionFactory;
    final ObjectMapper objectMapper;

    @Bean
    @ConditionalOnBean(IRedisPrefixConverter.class)
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(IRedisPrefixConverter redisPrefixConverter) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(this.redisConnectionFactory);
        template.setKeySerializer(new PrefixStringRedisSerializer(redisPrefixConverter));
        return template;
    }

    @Bean
    @ConditionalOnBean(IRedisPrefixConverter.class)
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(IRedisPrefixConverter redisPrefixConverter) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(this.redisConnectionFactory);
        // 设置  key 的序列化方式为 自定义 String Key 序列化
        template.setKeySerializer(new PrefixJdkRedisSerializer(redisPrefixConverter));
        // 设置 hash key 的序列化方式为 自定义 String Key 序列化
        template.setHashKeySerializer(new PrefixJdkRedisSerializer(redisPrefixConverter));
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
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
