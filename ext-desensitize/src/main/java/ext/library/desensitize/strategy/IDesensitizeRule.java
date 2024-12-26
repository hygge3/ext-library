package ext.library.desensitize.strategy;

import java.util.function.Function;

/**
 * 自定义数据脱敏可实现当前接口 或直接在 SensitiveStrategy 中添加枚举
 *
 */
@FunctionalInterface
public interface IDesensitizeRule {

	/**
	 * 脱敏操作
	 * @return {@link Function}<{@link String}, {@link String}>
	 */
	Function<String, String> desensitize();

}
