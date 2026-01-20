package ext.library.idempotent;

import org.aspectj.lang.JoinPoint;

/**
 * 幂等 Key 生成器接口
 * <p>
 * 负责根据切点信息和注解配置生成唯一的幂等标识。
 * 可通过实现此接口自定义 Key 生成策略。
 */
@FunctionalInterface
public interface KeyGenerator {

    /**
     * 生成幂等 key
     *
     * @param joinPoint  切点
     * @param idempotent 幂等注解
     * @return 幂等 key 标识
     */
    String generate(JoinPoint joinPoint, Idempotent idempotent);

}
