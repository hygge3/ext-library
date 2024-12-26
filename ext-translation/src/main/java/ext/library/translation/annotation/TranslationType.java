package ext.library.translation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ext.library.translation.service.TranslationInterface;

/**
 * 翻译类型注解 (标注到{@link TranslationInterface} 的实现类)
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface TranslationType {

	/**
	 * 类型
	 */
	String type();

}
