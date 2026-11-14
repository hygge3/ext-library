package ext.library.metrics.pool;

import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.core.MetricsSnapshot;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 池水位采样
 * <p>
 * 从容器里按 bean 类型抓线程池与连接池，不要求业务改代码。只认 {@code ThreadPoolTaskExecutor}、
 * 裸 {@code ThreadPoolExecutor} 与 HikariCP：别的池（Druid、Tomcat JDBC）要单独适配，不做通配。
 */
public class PoolSampler implements Runnable {

    private static final boolean HIKARI_PRESENT =
            ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource", PoolSampler.class.getClassLoader());

    private final MetricsRegistry registry;

    private final ListableBeanFactory beanFactory;

    public PoolSampler(MetricsRegistry registry, ListableBeanFactory beanFactory) {
        this.registry = registry;
        this.beanFactory = beanFactory;
    }

    @Override
    public void run() {
        beanFactory.getBeansOfType(ThreadPoolTaskExecutor.class).forEach(this::sampleTaskExecutor);
        beanFactory.getBeansOfType(ThreadPoolExecutor.class).forEach(this::sampleExecutor);
        if (HIKARI_PRESENT) {
            beanFactory.getBeansOfType(DataSource.class).forEach(this::sampleDataSource);
        }
    }

    private void sampleTaskExecutor(String name, ThreadPoolTaskExecutor taskExecutor) {
        ThreadPoolExecutor executor;
        try {
            executor = taskExecutor.getThreadPoolExecutor();
        } catch (IllegalStateException _) {
            // 还没初始化就先跳过，下一轮再采
            return;
        }
        sampleExecutor(name, executor);
    }

    private void sampleExecutor(String name, ThreadPoolExecutor executor) {
        registry.updatePool(new MetricsSnapshot.Pool(name,
                                                    "线程池",
                                                    executor.getActiveCount(),
                                                    executor.getPoolSize(),
                                                    executor.getMaximumPoolSize(),
                                                    executor.getQueue().size(),
                                                    executor.getQueue().size() + executor.getQueue().remainingCapacity()));
    }

    private void sampleDataSource(String name, DataSource dataSource) {
        if (!(dataSource instanceof com.zaxxer.hikari.HikariDataSource hikari)) {
            return;
        }
        try {
            registry.updatePool(new MetricsSnapshot.Pool(name,
                                                        "连接池",
                                                        hikari.getHikariPoolMXBean() == null ? 0 : hikari.getHikariPoolMXBean().getActiveConnections(),
                                                        hikari.getHikariPoolMXBean() == null ? 0 : hikari.getHikariPoolMXBean().getTotalConnections(),
                                                        hikari.getMaximumPoolSize(),
                                                        0,
                                                        0));
        } catch (RuntimeException _) {
            // 池还没启动时 MXBean 为空，等下一轮
        }
    }
}
