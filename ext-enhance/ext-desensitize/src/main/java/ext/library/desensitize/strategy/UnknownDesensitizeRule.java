package ext.library.desensitize.strategy;

import java.util.function.Function;

/**
 * 不处理数据脱敏
 *
 */
public class UnknownDesensitizeRule implements IDesensitizeRule {

	/**
	 * 脱敏操作
	 * @return {@link String}
	 */
	@Override
	public Function<String, String> desensitize() {
		return s -> s;
	}

}
