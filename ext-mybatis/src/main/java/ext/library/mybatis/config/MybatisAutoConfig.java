package ext.library.mybatis.config;

import jakarta.annotation.Nonnull;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.query.QueryColumnBehavior;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ext.library.mybatis.config.properties.MybatisProperties;
import ext.library.mybatis.util.TenantUtil;
import ext.library.tool.$;
import ext.library.tool.constant.Holder;
import ext.library.tool.constant.Symbol;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Mybatis-Flex 配置
 */
@RequiredArgsConstructor
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({MybatisProperties.class})
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisAutoConfig implements MyBatisFlexCustomizer {

    final MybatisProperties mybatisProperties;

    static {
        // 使用内置规则自动忽略 null、空白字符串、空集合
        QueryColumnBehavior.setIgnoreFunction($::isEmpty);
        // 如果传入的值是集合或数组，则使用 in 逻辑，否则使用 =（等于）逻辑
        QueryColumnBehavior.setSmartConvertInToEquals(true);
    }

    private static String formatSQL(@Nonnull String sql) {
        return sql.replaceAll("\\s+", Symbol.SPACE).replace("\\r", Symbol.SPACE).replace("\\n", Symbol.SPACE);
    }

    @Override
    public void customize(@Nonnull FlexGlobalConfig globalConfig) {
        // 全局配置逻辑删除字段
        globalConfig.setLogicDeleteColumn(mybatisProperties.getDeleteField());
        if (mybatisProperties.getSqlPrint()) {
            AuditManager.setAuditEnable(true);
            // SQL 打印
            AuditManager.setMessageCollector(auditMessage -> log.info("[🐦] RN:{},TTE:{}ms,SQL:{}", auditMessage.getQueryCount(), auditMessage.getElapsedTime(), formatSQL(auditMessage.getFullSql())));
        }
        if (mybatisProperties.getTenant()) {
            TenantManager.setTenantFactory(() -> {
                // 通过这里返回当前租户 ID
                return new Object[]{TenantUtil.get()};
            });
        }
    }

    /**
     * 初始化数据源
     * <p>
     * 本方法使用 HikariCP 连接池配置数据源它根据预设的配置参数初始化一个 HikariDataSource 对象
     * 配置包括最大连接池大小、最小空闲连接数、连接超时时间、连接生命周期、空闲释放时间等
     * 此外，还配置了一些特定的属性，如预编译语句缓存大小和 SQL 限制等，以优化数据库连接性能
     *
     * @return HikariDataSource 返回配置好的数据源对象
     */
    @Bean
    public HikariDataSource initDataSource() {
        // 创建 HikariCP 配置对象
        HikariConfig config = new HikariConfig();
        // 动态调整：CPU 核心数*2
        config.setMaximumPoolSize(Holder.CPU_CORE_NUM * 2);
        // 设置最小空闲连接数
        config.setMinimumIdle(5);
        // 3 秒超时
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(3));
        // 30 分钟
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        // 10 分钟空闲释放
        config.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
        // 设置连接泄漏检测阈值为 1 分钟
        config.setLeakDetectionThreshold(TimeUnit.MINUTES.toMillis(1));

        // 初始化数据源属性
        Properties dataSourceProperties = new Properties();
        // 设置预编译语句缓存大小为 250
        dataSourceProperties.setProperty("prepStmtCacheSize", "250");
        // 设置预编译语句 SQL 限制为 2048
        dataSourceProperties.setProperty("prepStmtCacheSqlLimit", "2048");
        // 启用预编译语句缓存
        dataSourceProperties.setProperty("cachePrepStmts", "true");
        // 启用服务器端预编译语句
        dataSourceProperties.setProperty("useServerPrepStmts", "true");
        // 将数据源属性应用到配置中
        config.setDataSourceProperties(dataSourceProperties);

        // 根据配置创建并返回 HikariDataSource 对象
        return new HikariDataSource(config);
    }


}
