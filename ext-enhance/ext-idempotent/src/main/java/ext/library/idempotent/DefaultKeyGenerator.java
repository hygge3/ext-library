package ext.library.idempotent;

import ext.library.core.util.spel.SpelUtil;
import ext.library.servlet.util.ServletUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 默认的幂等 Key 生成器
 * <p>
 * 支持 SpEL 表达式解析，可从方法参数和请求上下文中提取唯一标识。
 */
public class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String generate(JoinPoint joinPoint, Idempotent idempotent) {
        // 获取当前方法以及方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 没有填表达式时按「类名 + 方法名」退代：只拼 prefix 会让所有业务共用同一个常量 key，
        // 第一个请求就把所有业务锁死一个 duration
        String uniqueExpression = idempotent.uniqueExpression();
        if (uniqueExpression.isBlank()) {
            String target = joinPoint.getTarget() == null
                    ? method.getDeclaringClass().getName()
                    : joinPoint.getTarget().getClass().getName();
            return idempotent.prefix() + ":" + target + "#" + method.getName();
        }

        Object[] args = joinPoint.getArgs();
        // 根据当前切点，获取 SpEL 上下文
        StandardEvaluationContext spelContext = SpelUtil.getSpelContext(joinPoint.getTarget(), method, args);

        // 如果在 Servlet 环境下，将 request 放入上下文
        HttpServletRequest request = ServletUtil.getRequestOrNull();
        if (request != null) {
            spelContext.setVariable(RequestAttributes.REFERENCE_REQUEST, request);
        }

        // 解析出唯一标识并拼接完整 key
        String uniqueStr = SpelUtil.parseValueToString(spelContext, uniqueExpression);
        return idempotent.prefix() + ":" + uniqueStr;
    }

}
