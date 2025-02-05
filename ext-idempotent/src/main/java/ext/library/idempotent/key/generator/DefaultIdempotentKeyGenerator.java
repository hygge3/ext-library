package ext.library.idempotent.key.generator;

import java.lang.reflect.Method;

import ext.library.core.util.spel.SpelUtil;
import ext.library.idempotent.annotation.Idempotent;
import ext.library.tool.constant.Symbol;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 默认幂等 key 生成器
 */
public class DefaultIdempotentKeyGenerator implements IdempotentKeyGenerator {

    /**
     * 生成幂等 key
     *
     * @param joinPoint            切点
     * @param idempotentAnnotation 幂等注解
     * @return String 幂等标识
     */
    @Override
    public String generate(JoinPoint joinPoint,  Idempotent idempotentAnnotation) {
        String uniqueExpression = idempotentAnnotation.uniqueExpression();
        // 如果没有填写表达式，直接返回 prefix
        if (Symbol.EMPTY.equals(uniqueExpression)) {
            return idempotentAnnotation.prefix();
        }

        // 获取当前方法以及方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 根据当前切点，获取到 spEL 上下文
        StandardEvaluationContext spelContext = SpelUtil.getSpelContext(joinPoint.getTarget(), method, args);
        // 如果在 servlet 环境下，则将 request 信息放入上下文，便于获取请求参数
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes != null) {
            spelContext.setVariable(RequestAttributes.REFERENCE_REQUEST, requestAttributes.getRequest());
        }
        // 解析出唯一标识
        String uniqueStr = SpelUtil.parseValueToString(spelContext, uniqueExpression);
        // 和 prefix 拼接获得完整的 key
        return idempotentAnnotation.prefix() + Symbol.COLON + uniqueStr;
    }

}
