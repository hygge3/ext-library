package ext.library.lock;

import ext.library.core.util.spel.SpelUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 分布式锁 Key 生成器
 * <p>
 * 通过 SpEL 表达式从方法参数与请求上下文中提取锁的业务维度标识。
 */
@FunctionalInterface
public interface LockKeyGenerator {

    /**
     * 生成锁 key
     *
     * @param joinPoint 切点
     * @param redisLock 锁注解
     * @return 完整的锁 key
     */
    String generate(JoinPoint joinPoint, RedisLock redisLock);

    /**
     * 默认实现，支持 SpEL 表达式解析
     */
    class Default implements LockKeyGenerator {

        @Override
        public String generate(JoinPoint joinPoint, RedisLock redisLock) {
            // 定位到方法声明
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            // 构造 SpEL 上下文
            StandardEvaluationContext spelContext = SpelUtil.getSpelContext(joinPoint.getTarget(), method, args);

            // Servlet 环境下，将 request 放入上下文
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                spelContext.setVariable(RequestAttributes.REFERENCE_REQUEST, attributes.getRequest());
            }

            // 未配置表达式时，按「类名 + 方法名」加锁
            if (redisLock.key().isBlank()) {
                String target = joinPoint.getTarget() == null
                        ? method.getDeclaringClass().getName()
                        : joinPoint.getTarget().getClass().getName();
                return RedisLock.KEY_PREFIX + ":" + target + "#" + method.getName();
            }

            // 解析表达式并拼接完整 key
            return RedisLock.KEY_PREFIX + ":" + SpelUtil.parseValueToString(spelContext, redisLock.key());
        }

    }

}
