package ext.library.idempotent;

import ext.library.core.util.spel.SpelUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 默认的幂等 Key 生成器
 * <p>
 * 支持 SpEL 表达式解析，可从方法参数和请求上下文中提取唯一标识。
 */
public class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String generate(JoinPoint joinPoint, Idempotent idempotent) {
        String uniqueExpression = idempotent.uniqueExpression();

        // 如果没有填写表达式，直接返回 prefix
        if (uniqueExpression.isBlank()) {
            return idempotent.prefix();
        }

        // 获取当前方法以及方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        // 根据当前切点，获取 SpEL 上下文
        StandardEvaluationContext spelContext = SpelUtil.getSpelContext(joinPoint.getTarget(), method, args);

        // 如果在 Servlet 环境下，将 request 放入上下文
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            spelContext.setVariable(RequestAttributes.REFERENCE_REQUEST, requestAttributes.getRequest());
        }

        // 解析出唯一标识并拼接完整 key
        String uniqueStr = SpelUtil.parseValueToString(spelContext, uniqueExpression);
        return idempotent.prefix() + ":" + uniqueStr;
    }

}
