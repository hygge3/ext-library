package ext.library.metrics.jdbc;

import ext.library.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * SQL 耗时采集
 * <p>
 * 包住容器里的 {@link JdbcTemplate}：{@code JdbcClient} 由 boot 用这个 Bean 构建，所以
 * JdbcTemplate 与 JdbcClient 两条路径都覆盖。MyBatis 走 {@code SqlSessionTemplate}，不在范围内。
 */
public class SqlTimingBeanPostProcessor implements BeanPostProcessor {

    /** 真正发 SQL 的方法前缀 */
    private static final Set<String> SQL_METHODS = Set.of("query", "queryForObject", "queryForList", "queryForMap",
                                                          "queryForRowSet", "queryForStream", "update", "batchUpdate",
                                                          "execute", "call");

    private final ObjectProvider<MetricsRegistry> registryProvider;

    /**
     * @param registryProvider 指标容器；延迟取，避免提前初始化配置属性
     */
    public SqlTimingBeanPostProcessor(ObjectProvider<MetricsRegistry> registryProvider) {
        this.registryProvider = registryProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof JdbcTemplate template) || AopUtils.isAopProxy(bean)) {
            return bean;
        }
        ProxyFactory factory = new ProxyFactory(template);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new SqlTimingInterceptor(registryProvider));
        return factory.getProxy();
    }

    private record SqlTimingInterceptor(ObjectProvider<MetricsRegistry> registryProvider) implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            MetricsRegistry registry = registryProvider.getIfAvailable();
            Method method = invocation.getMethod();
            if (!SQL_METHODS.contains(method.getName())) {
                return invocation.proceed();
            }
            long start = System.nanoTime();
            try {
                return invocation.proceed();
            } finally {
                long millis = (System.nanoTime() - start) / 1_000_000;
                if (registry != null) {
                    registry.slowSql(sqlOf(invocation), millis);
                }
            }
        }

        /**
         * JdbcTemplate 的 SQL 一律是第一个参数；拿不到就退回方法名，至少能看出是哪类操作慢
         */
        private static String sqlOf(MethodInvocation invocation) {
            Object[] args = invocation.getArguments();
            return args.length > 0 && args[0] instanceof String sql ? sql : invocation.getMethod().getName();
        }
    }
}
