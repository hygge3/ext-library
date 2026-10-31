package ext.library.ratelimit;

import ext.library.core.util.spel.SpelUtil;
import ext.library.servlet.util.ServletUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 限流 Key 生成器
 * <p>
 * 通过 SpEL 表达式从方法参数与请求上下文中提取限流维度标识。
 */
@FunctionalInterface
public interface RateLimitKeyGenerator {

    /**
     * 生成限流 key
     *
     * @param joinPoint 切点
     * @param rateLimit 限流注解
     * @return 完整的限流 key
     */
    String generate(JoinPoint joinPoint, RateLimit rateLimit);

    /**
     * 默认实现，支持 SpEL 表达式解析
     */
    class Default implements RateLimitKeyGenerator {

        @Override
        public String generate(JoinPoint joinPoint, RateLimit rateLimit) {
            // 定位到方法声明
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            // 构造 SpEL 上下文
            StandardEvaluationContext spelContext = SpelUtil.getSpelContext(joinPoint.getTarget(), method, args);

            // Servlet 环境下，将 request 放入上下文
            HttpServletRequest request = ServletUtil.getRequestOrNull();
            if (request != null) {
                spelContext.setVariable(RequestAttributes.REFERENCE_REQUEST, request);
            }

            // 未配置表达式时，按「类名 + 方法名」整体限流
            if (rateLimit.key().isBlank()) {
                String target = joinPoint.getTarget() == null
                        ? method.getDeclaringClass().getName()
                        : joinPoint.getTarget().getClass().getName();
                return rateLimit.prefix() + ":" + target + "#" + method.getName();
            }

            // 解析表达式并拼接完整 key
            return rateLimit.prefix() + ":" + SpelUtil.parseValueToString(spelContext, rateLimit.key());
        }

    }

}
