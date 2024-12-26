package ext.library.desensitize.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ext.library.desensitize.handler.SensitiveHandler;
import ext.library.desensitize.strategy.IDesensitizeRule;
import ext.library.desensitize.strategy.SensitiveStrategy;
import ext.library.desensitize.strategy.UnknownDesensitizeRule;

/**
 * 自定义 jackson 注解，标注在属性上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveHandler.class)
public @interface Sensitive {

	/**
	 * 策略
	 * @return {@link SensitiveStrategy}
	 */
	SensitiveStrategy strategy() default SensitiveStrategy.DEFAULT;

	/**
	 * 是否自定义规则
	 * @return boolean
	 */
	boolean isCustomRule() default false;

	/**
	 * 自定义规则实现类
	 * @return {@link Class}<{@link ?} {@link extends} {@link IDesensitizeRule}>
	 */
	Class<? extends IDesensitizeRule> customRule() default UnknownDesensitizeRule.class;

}
