package ext.library.logback.config;

import ext.library.logback.mask.LogMaskRegistry;
import ext.library.logback.mask.LogMaskRule;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 日志脱敏自动配置
 * <p>
 * 启动时把脱敏规则写入 {@link LogMaskRegistry}，供 logback 的 {@code %maskedMsg} 转换器读取。
 * converter 由 logback XML 实例化、拿不到 Spring 容器，所以这里通过静态注册表传递规则。
 */
@AutoConfiguration
@EnableConfigurationProperties(LogMaskProperties.class)
public class LogMaskAutoConfiguration {

    /**
     * 容器就绪后写入规则集
     *
     * @param properties 脱敏配置
     * @return 初始化器
     */
    @Bean
    public LogMaskInitializer logMaskInitializer(LogMaskProperties properties) {
        return new LogMaskInitializer(properties);
    }

    /**
     * 应用启动时初始化脱敏规则集
     */
    public static final class LogMaskInitializer implements InitializingBean {

        /** 脱敏配置 */
        private final LogMaskProperties properties;

        private LogMaskInitializer(LogMaskProperties properties) {
            this.properties = properties;
        }

        @Override
        public void afterPropertiesSet() {
            List<LogMaskRule> rules = LogMaskRuleSets.resolve(properties);
            LogMaskRegistry.setRules(rules);
            Logs.info(EmojiSymbol.LOGBACK, "载入模块：日志脱敏（{} 条规则）", rules.size());
        }

    }

}
