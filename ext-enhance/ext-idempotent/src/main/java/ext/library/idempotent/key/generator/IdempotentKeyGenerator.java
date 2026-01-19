package ext.library.idempotent.key.generator;

import ext.library.idempotent.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;

/**
 * 幂等 key 生成器
 */
@FunctionalInterface
public interface IdempotentKeyGenerator {

	/**
	 * 生成幂等 key
	 * @param joinPoint 切点
	 * @param idempotentAnnotation 幂等注解
	 * @return 幂等 key 标识
	 */
	String generate(JoinPoint joinPoint, Idempotent idempotentAnnotation);

}
