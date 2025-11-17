package ext.library.mybatis.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.query.QueryColumnBehavior;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import ext.library.mybatis.config.properties.MybatisProperties;
import ext.library.mybatis.util.TenantUtil;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.Serializable;

/**
 * Mybatis-Flex 配置
 */
@AutoConfiguration
@EnableConfigurationProperties({MybatisProperties.class})
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisAutoConfig implements MyBatisFlexCustomizer {
    static {
        // 使用内置规则自动忽略 null、空白字符串、空集合
        QueryColumnBehavior.setIgnoreFunction(ObjectUtil::isEmpty);
        // 如果传入的值是集合或数组，则使用 in 逻辑，否则使用 =（等于）逻辑
        QueryColumnBehavior.setSmartConvertInToEquals(true);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MybatisProperties mybatisProperties;

    public MybatisAutoConfig(MybatisProperties mybatisProperties) {
        this.mybatisProperties = mybatisProperties;
    }

    private static String formatSQL(String sql) {
        return sql.replaceAll("\\s+", Symbol.SPACE).replace("\\r", Symbol.SPACE).replace("\\n", Symbol.SPACE);
    }

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        // 全局配置逻辑删除字段
        globalConfig.setLogicDeleteColumn(mybatisProperties.getDeleteField());
        if (mybatisProperties.getSqlPrint()) {
            AuditManager.setAuditEnable(true);
            // SQL 打印
            AuditManager.setMessageCollector(auditMessage -> log.info("[🐦] RN:{},TTE:{}ms,SQL:{}", auditMessage.getQueryCount(), auditMessage.getElapsedTime(), formatSQL(auditMessage.getFullSql())));
        }
        if (mybatisProperties.getTenant()) {
            final ScopedValue<Serializable> TENANT_ID = ScopedValue.newInstance();

            TenantManager.setTenantFactory(() -> {
                try {
                    // 通过这里返回当前租户 ID
                    return new Object[]{TenantUtil.get()};
                } finally {
                    TenantUtil.clear();
                }
            });
        }
        log.info("[🐦] MyBatis 模块载入成功");
    }

}