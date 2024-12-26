package ext.library.core.config;

import java.util.Arrays;
import java.util.concurrent.Executor;

import ext.library.core.util.SpringUtil;
import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * 异步配置
 * <p>
 * 如果未使用虚拟线程则生效
 */
@Slf4j
@AutoConfiguration
public class AsyncConfig implements AsyncConfigurer {

	/**
	 * 自定义 @Async 注解使用系统线程池
	 */
	@Override
	public Executor getAsyncExecutor() {
		if (SpringUtil.isVirtual()) {
			return new VirtualThreadTaskExecutor("async-");
		}
		return SpringUtil.getBean("scheduledExecutorService");
	}

	/**
	 * 异步执行异常处理
	 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (throwable, method, objects) -> {
			Exceptions.print(throwable);
			StringBuilder sb = new StringBuilder();
			sb.append("Exception message - ")
				.append(throwable.getMessage())
				.append(", Method name - ")
				.append(method.getName());
			if ($.isNotEmpty(objects)) {
				sb.append(", Parameter value - ").append(Arrays.toString(objects));
			}
			throw new RuntimeException(sb.toString());
		};
	}

}
