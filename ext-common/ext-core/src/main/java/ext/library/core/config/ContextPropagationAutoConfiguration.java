package ext.library.core.config;

import ext.library.core.context.ContextTaskDecorator;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.trace.TraceContext;
import java.lang.reflect.Field;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 上下文传播自动配置
 * <p>
 * 给容器里所有 {@link ThreadPoolTaskExecutor} 挂上 {@link ContextTaskDecorator}，
 * 让 MDC 与数据权限范围跟着任务进线程池；给 {@link ThreadPoolTaskScheduler} 挂上生成新 traceId 的装饰器，
 * 让定时任务也有链路。要在 {@code afterPropertiesSet} 之前设置，否则线程池已建好，装饰器不生效。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
public class ContextPropagationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextTaskDecorator contextTaskDecorator() {
        return new ContextTaskDecorator();
    }

    /**
     * 为线程池挂载装饰器
     * <p>
     * {@link ThreadPoolTaskExecutor} 没有读取 taskDecorator 的 getter，无法判断消费方是否已配置，
     * 因此这里会直接覆盖并打一条 WARN —— 静默覆盖会让对方的上下文传播莫名失效。
     * <p>
     * 装饰器用 {@link ObjectProvider} 延迟取：直接声明成方法参数会让本类与装饰器在
     * {@code contextTaskDecoratorPostProcessor} 创建时就提前实例化，错过后续的 BeanPostProcessor
     * （Spring 会打 "not eligible for getting processed by all BeanPostProcessors"）。
     *
     * @param decoratorProvider 上下文装饰器（延迟解析）
     *
     * @return 后处理器
     */
    @Bean
    public static BeanPostProcessor contextTaskDecoratorPostProcessor(
            ObjectProvider<ContextTaskDecorator> decoratorProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
                    throws BeansException {
                if (bean instanceof ThreadPoolTaskExecutor executor) {
                    ContextTaskDecorator decorator = decoratorProvider.getObject();
                    TaskDecorator existing = currentDecorator(executor);
                    executor.setTaskDecorator(decorator);
                    // 只在自己真的覆盖了别人的装饰器时才吵；没有 getter，只能反射读私有字段
                    if (existing != null && existing != decorator) {
                        Logs.warn(EmojiSymbol.CORE,
                                "已为线程池 {} 设置上下文传播装饰器，原有的 TaskDecorator 会被覆盖", beanName);
                    }
                }
                return bean;
            }
        };
    }

    /**
     * 读线程池当前的 TaskDecorator
     * <p>
     * {@link ThreadPoolTaskExecutor} 没有 getter，只能反射读私有字段；读不到（字段改名或不可访问）就当没有，
     * 最坏结果是少打一条覆盖警告。
     *
     * @param executor 线程池
     *
     * @return 现有装饰器，没有则 {@code null}
     */
    private static @Nullable TaskDecorator currentDecorator(ThreadPoolTaskExecutor executor) {
        try {
            Field field = ThreadPoolTaskExecutor.class.getDeclaredField("taskDecorator");
            field.setAccessible(true);
            return (TaskDecorator) field.get(executor);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /**
     * 给定时任务调度器挂上「一条任务一条新链路」的装饰器
     * <p>
     * {@code @Scheduled} 没有入站请求，MDC 里没有 traceId：任务日志串不起来，内部的出站请求也带不上追踪头。
     * 这里在每次任务执行前生成 traceId，执行后还原。
     *
     * @return 后处理器
     */
    @Bean
    @ConditionalOnClass(ThreadPoolTaskScheduler.class)
    public static BeanPostProcessor traceTaskSchedulerPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
                    throws BeansException {
                if (bean instanceof ThreadPoolTaskScheduler scheduler) {
                    scheduler.setTaskDecorator(TraceContext::wrap);
                }
                return bean;
            }
        };
    }

}
