package ext.library.redis.config;

import ext.library.redis.prefix.RedisPrefixConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 前缀转换器与两个模板 bean 必须真正注册：曾因 {@code @ConditionalOnBean} 引用同类的属性/转换器而全部失效，
 * 导致 {@code ext.redis.key-prefix} 配了也不生效。
 */
class RedisAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
            .withBean(JsonMapper.class, JsonMapper::new)
            .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class));

    @Test
    void 前缀转换器与模板被注册且key带前缀() {
        runner.withPropertyValues("ext.redis.key-prefix=myapp:").run(context -> {
            assertThat(context).hasSingleBean(RedisPrefixConverter.class);
            assertThat(context).hasSingleBean(StringRedisTemplate.class);
            assertThat(serializeKey(context.getBean(StringRedisTemplate.class), "user:1")).isEqualTo("myapp:user:1");
        });
    }

    @Test
    void 未配置前缀时key保持原样() {
        runner.run(context ->
                assertThat(serializeKey(context.getBean(StringRedisTemplate.class), "user:1")).isEqualTo("user:1"));
    }

    private static String serializeKey(StringRedisTemplate template, String key) {
        @SuppressWarnings("unchecked")
        RedisSerializer<Object> keySerializer = (RedisSerializer<Object>) template.getKeySerializer();
        return new String(keySerializer.serialize(key), StandardCharsets.UTF_8);
    }

}
