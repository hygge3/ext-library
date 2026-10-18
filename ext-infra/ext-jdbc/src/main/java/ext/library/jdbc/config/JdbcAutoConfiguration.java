package ext.library.jdbc.config;

import ext.library.jdbc.JdbcDaoFactory;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * JdbcDao 自动配置
 * <p>
 * 依赖 {@code spring-boot-starter-jdbc} 与容器中的 {@link JdbcClient}。
 * {@link JdbcClient} 是可选依赖，因此条件注解必须使用字符串形式判断。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.jdbc.core.simple.JdbcClient")
public class JdbcAutoConfiguration {

    /**
     * 通用 DAO 工厂
     *
     * @param jdbcClient JdbcClient
     *
     * @return DAO 工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public JdbcDaoFactory jdbcDaoFactory(JdbcClient jdbcClient) {
        Logs.info(EmojiSymbol.JDBC, "载入模块：通用 JDBC DAO");
        return new JdbcDaoFactory(jdbcClient);
    }

}
