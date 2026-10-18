package ext.library.audit.config;

import ext.library.audit.AuditDispatcher;
import ext.library.audit.AuditHandler;
import ext.library.audit.AuditAspect;
import ext.library.audit.AuditOperatorResolver;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 操作审计自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {

    /**
     * 默认的操作人解析器
     *
     * @return 操作人解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditOperatorResolver auditOperatorResolver() {
        return new AuditOperatorResolver.Default();
    }

    /**
     * 审计记录分发器
     * <p>
     * 容器内所有 {@link AuditHandler} 都会被收集；一个都没有时退化为默认处理器，仅打日志。
     *
     * @param handlers  业务注册的审计处理器
     * @param properties 审计配置
     * @return 分发器
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditDispatcher auditDispatcher(ObjectProvider<AuditHandler> handlers, AuditProperties properties) {
        List<AuditHandler> list = handlers.orderedStream().toList();
        if (list.isEmpty() && properties.isLogToConsole()) {
            list = List.of(record -> Logs.info(EmojiSymbol.AUDIT,
                    "操作审计：{} | {} | {} | 操作人={} | 耗时={}ms | 成功={}",
                    record.module(), record.type(), record.desc(), record.operator(),
                    record.duration().toMillis(), record.success()));
        }
        Logs.info(EmojiSymbol.AUDIT, "载入模块：操作审计（{} 个处理器）", list.size());
        return new AuditDispatcher(list);
    }

    /**
     * 审计切面
     *
     * @param dispatcher        分发器
     * @param operatorResolver  操作人解析器
     * @return 审计切面
     */
    @Bean
    public AuditAspect auditAspect(AuditDispatcher dispatcher, AuditOperatorResolver operatorResolver) {
        return new AuditAspect(dispatcher, operatorResolver);
    }

}
