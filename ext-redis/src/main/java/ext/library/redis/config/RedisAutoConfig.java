package ext.library.redis.config;

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

/**
 * Redis 自动配置类
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfig {

    private final RedisConnectionFactory redisConnectionFactory;

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
        template.setKeySerializer(new PrefixJdkRedisSerializer(redisPrefixConverter));
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer result = new RedisMessageListenerContainer();
        result.setConnectionFactory(redisConnectionFactory);
        return result;
    }

}
