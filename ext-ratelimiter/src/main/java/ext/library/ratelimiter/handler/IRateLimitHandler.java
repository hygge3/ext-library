package ext.library.ratelimiter.handler;

import ext.library.core.util.ServletUtil;
import ext.library.core.util.SpringUtil;
import ext.library.core.util.spel.SpelUtil;
import ext.library.ratelimiter.annotation.RateLimit;
import ext.library.tool.constant.Symbol;
import ext.library.tool.holder.Lazy;
import ext.library.tool.util.StringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Redis 速率限制处理器
 */

public interface IRateLimitHandler {
    Logger log = LoggerFactory.getLogger(IRateLimitHandler.class);
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
    Lazy<String> RATE_LIMIT_KEY = Lazy.of(() -> SpringUtil.getProperty("ext.limiter.key-prefix", "ext.rate_limit"));

    /**
     * 执行
     *
     * @param rateLimit 速率限制
     * @param point     点
     *
     * @return boolean
     */
    boolean proceed(RateLimit rateLimit, JoinPoint point);

    /**
     * 构建唯一标示 KEY
     *
     * @param rateLimit 速率限制器
     * @param point     观点
     *
     * @return {@code String }
     */
    default String getCombineKey(RateLimit rateLimit, JoinPoint point) {
        String key = rateLimit.key();
        if (StringUtil.isNotBlank(key)) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            Object[] args = point.getArgs();
            key = SpelUtil.parseValueToString(point, targetMethod, args, key);
        }
        HttpServletRequest request = ServletUtil.getRequest();
        String finalKey = String.join(Symbol.COLON, RATE_LIMIT_KEY.get(), request.getRequestURI(), ServletUtil.getIpAddr(request), key);
        if (log.isDebugEnabled()) {
            log.debug("[🚥] rate.limit.key:{}", finalKey);
        }
        return finalKey;
    }
}