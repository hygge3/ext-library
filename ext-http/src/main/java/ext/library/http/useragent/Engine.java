package ext.library.http.useragent;

import java.io.Serial;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 引擎对象
 */
public class Engine extends UserAgentInfo {

	@Serial
	 static final long serialVersionUID = 1L;

	/** 未知 */
	public static final Engine Unknown = new Engine(NameUnknown, null);

	/**
	 * 支持的引擎类型
	 */
	public static final List<Engine> engines = List.of(new Engine("Trident", "trident"), new Engine("Webkit", "webkit"),
			new Engine("Chrome", "chrome"), new Engine("Opera", "opera"), new Engine("Presto", "presto"),
			new Engine("Gecko", "gecko"), new Engine("KHTML", "khtml"), new Engine("Konqueror", "konqueror"),
			new Engine("MIDP", "MIDP"));

	 final Pattern versionPattern;

	/**
	 * 构造
	 * @param name 引擎名称
	 * @param regex 关键字或表达式
	 */
	public Engine(String name, String regex) {
		super(name, regex);
		this.versionPattern = Pattern.compile(name + "[/\\- ]([\\d\\w.\\-]+)", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * 获取引擎版本
	 * @param userAgentString User-Agent 字符串
	 * @return 版本
	 * @since 5.7.4
	 */
	public String getVersion(String userAgentString) {
		if (isUnknown()) {
			return null;
		}
		Matcher m = this.versionPattern.matcher(userAgentString);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

}
