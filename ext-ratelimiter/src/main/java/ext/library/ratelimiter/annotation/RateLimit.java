package ext.library.ratelimiter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 速率限制注解类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RateLimit {

    /**
     * 限流 key，支持使用 Spring EL 表达式来动态获取方法上的参数值 格式类似于 #code.id #{#code}
     */
    String key() default "";

    /**
     * 限定阈值
     * <p>
     * 时间间隔 interval 范围内超过该数量会触发锁
     */
    long count();

    /**
     * 时间间隔，默认 3 分钟
     * <p>
     * 例如 5s 五秒，6m 六分钟，7h 七小时，8d 八天
     */
    String interval() default "3m";

    /**
     * 限制策略
     */
    String[] strategy() default {};

    /**
     * 得不到令牌的提示语
     */
    String msg() default "流量超出最大限制";

}