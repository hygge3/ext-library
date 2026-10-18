package ext.library.audit;

import ext.library.core.util.spel.SpelUtil;
import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 操作审计切面
 * <p>
 * 拦截标注了 {@link OperationLog} 的方法，采集操作人、入参、耗时与执行结果，
 * 无论成败都会投递给 {@link AuditDispatcher}。
 */
@Aspect
public record AuditAspect(AuditDispatcher dispatcher, AuditOperatorResolver operatorResolver) {

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 组装审计上下文
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        StandardEvaluationContext spelContext =
                SpelUtil.getSpelContext(joinPoint.getTarget(), method, joinPoint.getArgs());
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            spelContext.setVariable(RequestAttributes.REFERENCE_REQUEST, attributes.getRequest());
        }

        String module = operationLog.module().isBlank()
                ? method.getDeclaringClass().getSimpleName() + "#" + method.getName()
                : operationLog.module();
        String desc = operationLog.desc().isBlank()
                ? ""
                : SpelUtil.parseValueToString(spelContext, operationLog.desc());

        // 执行被拦截的方法，无论成败都要留痕
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable failure = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            failure = e;
            throw e;
        } finally {
            try {
                dispatcher.dispatch(buildRecord(
                        joinPoint, operationLog, module, desc, result, failure, System.currentTimeMillis() - start
                ));
            } catch (RuntimeException e) {
                Logs.warn(EmojiSymbol.AUDIT, e, "审计记录组装失败，module: {}", module);
            }
        }
    }

    /**
     * 组装审计记录
     *
     * @param joinPoint    切点
     * @param operationLog 审计注解
     * @param module       模块名
     * @param desc         操作描述
     * @param result       方法返回值
     * @param failure      抛出的异常
     * @param cost         执行耗时（毫秒）
     * @return 审计记录
     */
    private AuditRecord buildRecord(
            ProceedingJoinPoint joinPoint,
            OperationLog operationLog,
            String module,
            String desc,
            Object result,
            Throwable failure,
            long cost
    ) {
        return new AuditRecord(
                operatorResolver.resolve(),
                module,
                operationLog.type(),
                desc,
                operationLog.recordArgs() ? JsonUtil.toJson(joinPoint.getArgs()) : null,
                operationLog.recordResult() ? JsonUtil.toJson(result) : null,
                failure == null,
                failure == null ? null : failure.getClass().getSimpleName() + ": " + failure.getMessage(),
                Duration.ofMillis(cost),
                LocalDateTime.now()
        );
    }

}
