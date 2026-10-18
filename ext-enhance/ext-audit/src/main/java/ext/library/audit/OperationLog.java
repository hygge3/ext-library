package ext.library.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计注解
 * <p>
 * 标注在需要留痕的方法上，切面会在方法执行前后记录操作人、入参、耗时与执行结果，
 * 并把审计记录投递给所有 {@link AuditHandler}。
 * <p>
 * 使用示例：
 * <pre>{@code
 * &#64;OperationLog(module = "订单", type = OperationType.UPDATE, desc = "提交订单")
 * public Result<Void> submit(Order order) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 业务模块名
     * <p>
     * 留空时取「类名 + 方法名」。
     *
     * @return 模块名
     */
    String module() default "";

    /**
     * 操作类型
     *
     * @return 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 操作描述
     * <p>
     * 支持 SpEL，可从方法参数中引用，如 {@code "下单商品 #orderId"}。
     *
     * @return 描述
     */
    String desc() default "";

    /**
     * 是否记录方法入参
     *
     * @return 是否记录入参
     */
    boolean recordArgs() default true;

    /**
     * 是否记录方法返回值
     *
     * @return 是否记录返回值
     */
    boolean recordResult() default false;

}
