package ext.library.mybatis.config;

import java.util.Properties;

import com.github.pagehelper.PageInterceptor;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.logicdelete.LogicDeleteProcessor;
import com.mybatisflex.core.logicdelete.impl.DateTimeLogicDeleteProcessor;
import com.mybatisflex.core.query.QueryColumnBehavior;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import ext.library.mybatis.config.properties.MybatisProperties;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Mybatis-Flex 配置
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
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

    private static String formatSQL(@Language("sql") String sql) {
        return sql.replaceAll("\\s+", Symbol.SPACE).replace("\\r", Symbol.SPACE).replace("\\n", Symbol.SPACE);
    }

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        // 全局配置逻辑删除字段
        globalConfig.setLogicDeleteColumn(mybatisProperties.getDeleteField());
        if (mybatisProperties.getSqlPrint()) {
            AuditManager.setAuditEnable(true);
            // SQL 打印
            AuditManager.setMessageCollector(auditMessage -> log.info("[🐦] result:{},take:{}ms,{}", auditMessage.getQueryCount(), auditMessage.getElapsedTime(), formatSQL(auditMessage.getFullSql())));
        }
    }

    @Bean
    public LogicDeleteProcessor logicDeleteProcessor() {
        return new DateTimeLogicDeleteProcessor();
    }

    /**
     * 页面拦截器 使用自动配置无效，需要手动注入 bean，以让
     * com.mybatisflex.spring.boot.MybatisFlexAutoConfiguration#MybatisFlexAutoConfiguration
     * 正常获取到 com.github.pagehelper.PageInterceptor
     *
     * @return {@link PageInterceptor}
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        // 支持通过 Mapper 接口参数来传递分页参数，默认值 false，分页插件会从查询方法的参数值中，自动根据上面 params
        // 配置的字段中取值，查找到合适的值时就会自动分页。
        properties.put("supportMethodsArguments", "true");
        // 分页合理化参数，默认值为 false。当该参数设置为 true 时，pageNum<=0
        // 时会查询第一页，pageNum>pages（超过总数时），会查询最后一页。默认 false 时，直接根据参数进行查询。
        properties.put("reasonable", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

}
