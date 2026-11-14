package ext.library.core.context;

import ext.library.core.datascope.DataScopeContext;
import ext.library.core.datascope.DataScopeMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 跨线程上下文传播装饰器
 * <p>
 * 把提交任务时的 MDC 与数据权限范围带到线程池线程，执行后恢复原值。
 * 两者都放在这里而不是各自模块：同一个线程池只能挂一个 {@link TaskDecorator}，
 * 分两处注册会互相覆盖。
 *
 * @since 4.1.0
 */
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public @NonNull Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        DataScopeMeta scope = DataScopeContext.get();
        return () -> {
            Map<String, String> previousMdc = MDC.getCopyOfContextMap();
            DataScopeMeta previousScope = DataScopeContext.get();
            try {
                applyMdc(mdc);
                applyScope(scope);
                runnable.run();
            } finally {
                applyMdc(previousMdc);
                applyScope(previousScope);
            }
        };
    }

    private static void applyMdc(@Nullable Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }

    private static void applyScope(@Nullable DataScopeMeta scope) {
        if (scope == null) {
            DataScopeContext.clear();
        } else {
            DataScopeContext.set(scope);
        }
    }

}
