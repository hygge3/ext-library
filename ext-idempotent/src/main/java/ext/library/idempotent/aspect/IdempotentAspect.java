package ext.library.idempotent.aspect;

import ext.library.idempotent.annotation.Idempotent;
import ext.library.idempotent.key.generator.IdempotentKeyGenerator;
import ext.library.idempotent.key.store.IdempotentKeyStore;
import ext.library.tool.core.Exceptions;
import ext.library.tool.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;


/**
 * 幂等切面
 */
@Aspect
@RequiredArgsConstructor
public class IdempotentAspect {

    private final IdempotentKeyStore idempotentKeyStore;

    private final IdempotentKeyGenerator idempotentKeyGenerator;

    @Around("@annotation(idempotentAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotentAnnotation) throws Throwable {
        // 获取幂等标识
        String idempotentKey = this.idempotentKeyGenerator.generate(joinPoint, idempotentAnnotation);

        // 校验当前请求是否重复请求
        boolean saveSuccess = this.idempotentKeyStore.saveIfAbsent(idempotentKey, DateUtil.convert(idempotentAnnotation.duration(), idempotentAnnotation.timeUnit()));
        Assert.isTrue(saveSuccess, () -> {
            throw Exceptions.throwOut(idempotentAnnotation.message());
        });

        try {
            Object result = joinPoint.proceed();
            if (idempotentAnnotation.removeKeyWhenFinished()) {
                this.idempotentKeyStore.remove(idempotentKey);
            }
            return result;
        } catch (Throwable e) {
            // 异常时，根据配置决定是否删除幂等 key
            if (idempotentAnnotation.removeKeyWhenError()) {
                this.idempotentKeyStore.remove(idempotentKey);
            }
            throw e;
        }

    }

}