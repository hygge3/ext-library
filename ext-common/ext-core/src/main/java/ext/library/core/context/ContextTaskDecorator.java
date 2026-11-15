package ext.library.core.context;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 跨线程上下文传播装饰器
 * <p>
 * 把提交任务时的 MDC 带到线程池线程，执行后恢复原值。
 *
 * @since 4.1.0
 */
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public @NonNull Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previousMdc = MDC.getCopyOfContextMap();
            try {
                applyMdc(mdc);
                runnable.run();
            } finally {
                applyMdc(previousMdc);
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

}
