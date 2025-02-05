package ext.library.ratelimiter.handler;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;

import ext.library.core.util.ServletUtil;
import ext.library.core.util.SpringUtil;
import ext.library.ratelimiter.annotation.RateLimiter;
import ext.library.redis.constant.RedisKey;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Redis 速率限制处理器
 */
public interface IRateLimitHandler {

    /**
     * 定义 spel 表达式解析器
     */
    ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 定义 spel 解析模版
     */
    ParserContext PARSER_CONTEXT = new TemplateParserContext();

    /**
     * 方法参数解析器
     */
    ParameterNameDiscoverer PND = new DefaultParameterNameDiscoverer();

    boolean proceed(RateLimiter rateLimiter, JoinPoint point);

    /**
     * 构建唯一标示 KEY
     *
     * @param rateLimiter 速率限制器
     * @param point       观点
     * @return {@code String }
     */
    default String getCombineKey( RateLimiter rateLimiter, JoinPoint point) {
        String key = rateLimiter.key();
        if ($.isNotBlank(key)) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            Object[] args = point.getArgs();
            // noinspection DataFlowIssue
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, targetMethod, args, PND);
            context.setBeanResolver(new BeanFactoryResolver(SpringUtil.getBeanFactory()));
            Expression expression;
            if (key.startsWith(PARSER_CONTEXT.getExpressionPrefix())
                && key.endsWith(PARSER_CONTEXT.getExpressionSuffix())) {
                expression = PARSER.parseExpression(key, PARSER_CONTEXT);
            } else {
                expression = PARSER.parseExpression(key);
            }
            key = expression.getValue(context, String.class);
        }
        HttpServletRequest request = ServletUtil.getRequest();
        return RedisKey.RATE_LIMIT_KEY + request.getRequestURI() + Symbol.COLON + ServletUtil.getIpAddr(request) + Symbol.COLON
               + key;
    }

}
