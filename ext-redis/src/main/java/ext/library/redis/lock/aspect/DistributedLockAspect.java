package ext.library.redis.lock.aspect;

import java.lang.reflect.Method;
import java.util.Objects;

import ext.library.redis.lock.DistributedLockUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.intellij.lang.annotations.Language;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 分布式锁切面
 */
@Aspect
@Slf4j
@AutoConfiguration(after = RedisUtil.class)
public class DistributedLockAspect {

    /**
     * SpEL 表达式解析
     */
    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    /**
     * 用于获取方法参数名字
     */
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(ext.library.redis.lock.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 获取 DistributedLock
        ext.library.redis.lock.annotation.DistributedLock distributedLock = this.getDistributedLock(pjp);
        // 获取 lockKey
        String lockKey = this.getLockKey(pjp, distributedLock);
        try {
            Boolean lock = DistributedLockUtil.lock(lockKey, distributedLock.lockTime(), distributedLock.unit());
            if (!lock) {
                throw Exceptions.throwOut("对方法的重复请求仍在处理中");
            }
            return pjp.proceed();
        } finally {
            // 解锁
            this.unLock(lockKey);
        }
    }

    /**
     * 获取分布式锁
     *
     * @param pjp pjp
     * @return {@link ext.library.redis.lock.annotation.DistributedLock}
     * @throws NoSuchMethodException 没有这样方法例外
     */
    private ext.library.redis.lock.annotation.DistributedLock getDistributedLock(ProceedingJoinPoint pjp)
            throws NoSuchMethodException {
        String methodName = pjp.getSignature().getName();
        Class<?> clazz = pjp.getTarget().getClass();
        Class<?>[] par = ((MethodSignature) pjp.getSignature()).getParameterTypes();
        Method lockMethod = clazz.getMethod(methodName, par);
        return lockMethod.getAnnotation(ext.library.redis.lock.annotation.DistributedLock.class);
    }

    /**
     * 解锁
     *
     * @param lockKey 锁
     */
    private void unLock(String lockKey) {
        if (Objects.isNull(lockKey)) {
            return;
        }
        try {
            DistributedLockUtil.unLock(lockKey);
        } catch (Exception e) {
            log.error("[🔒] 分布式锁解锁异常", e);
        }
    }

    /**
     * 获取 lockKey
     *
     * @param pjp             pjp
     * @param distributedLock 分布式锁
     * @return {@link String}
     */
    private String getLockKey(ProceedingJoinPoint pjp,
                              ext.library.redis.lock.annotation.DistributedLock distributedLock) {
        @Language("spel")
        String lockKey = distributedLock.key();
        String keyPrefix = distributedLock.keyPrefix();
        if ($.isBlank(lockKey)) {
            throw Exceptions.throwOut("Lok key cannot be empty");
        }

        if (lockKey.contains("#")) {
            this.checkSpEL(lockKey);
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            // 获取方法参数值
            Object[] args = pjp.getArgs();
            lockKey = getValBySpEL(lockKey, methodSignature, args);
        }
        lockKey = $.isBlank(keyPrefix) ? lockKey : keyPrefix + lockKey;
        return lockKey;
    }

    /**
     * 解析 spEL 表达式
     *
     * @param spEL            sp el
     * @param methodSignature 方法签名
     * @param args            args
     * @return {@link String}
     */
    private String getValBySpEL(String spEL, MethodSignature methodSignature, Object[] args) {
        // 获取方法形参名数组
        String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
        if (paramNames == null || paramNames.length < 1) {
            throw Exceptions.throwOut("Lock key 不能为空");
        }
        Expression expression = spelExpressionParser.parseExpression(spEL);
        // spring 的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 给上下文赋值
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return expression.getValue(context).toString();
    }

    /**
     * SpEL 表达式校验
     *
     * @param spEL sp el
     */
    private void checkSpEL(@Language("spel") String spEL) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            parser.parseExpression(spEL, new TemplateParserContext());
        } catch (Exception e) {
            throw Exceptions.unchecked(e);
        }
    }

}
