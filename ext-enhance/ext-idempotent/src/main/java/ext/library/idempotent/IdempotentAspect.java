package ext.library.idempotent;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 幂等性控制切面
 * <p>
 * 通过 AOP 拦截标注了 {@link Idempotent} 注解的方法，实现幂等性控制。
 */
@Aspect
public record IdempotentAspect(KeyStore keyStore, KeyGenerator keyGenerator) {

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 生成幂等 key
        String idempotentKey = keyGenerator.generate(joinPoint, idempotent);

        // 尝试保存 key，若已存在则说明是重复请求
        boolean saveSuccess = keyStore.saveIfAbsent(
                idempotentKey,
                DateUtil.convert(idempotent.duration(), idempotent.timeUnit())
        );

        if (!saveSuccess) {
            throw new ExtException(EmojiSymbol.IDEMPOTENT, idempotent.message());
        }

        try {
            Object result = joinPoint.proceed();

            // 业务完成后，根据配置决定是否删除幂等 key
            if (idempotent.removeKeyWhenFinished()) {
                keyStore.remove(idempotentKey);
            }

            return result;
        } catch (Throwable e) {
            // 异常时，根据配置决定是否删除幂等 key
            if (idempotent.removeKeyWhenError()) {
                keyStore.remove(idempotentKey);
            }
            throw e;
        }
    }

}
