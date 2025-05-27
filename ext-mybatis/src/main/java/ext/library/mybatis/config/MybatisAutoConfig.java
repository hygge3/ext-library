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
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    public HikariDataSource initDataSource() {
        HikariConfig config = new HikariConfig();
        // 动态调整：CPU 核心数*2
        config.setMaximumPoolSize(Holder.CPU_CORE_NUM * 2);
        config.setMinimumIdle(5);
        // 3 秒超时
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(3));
        // 30 分钟
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        // 10 分钟空闲释放
        config.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
        return new HikariDataSource(config);
    }


}
