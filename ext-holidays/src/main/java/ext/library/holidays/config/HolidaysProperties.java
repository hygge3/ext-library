package ext.library.holidays.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holidays 配置类
 */
@Getter
@Setter
@ConfigurationProperties(HolidaysProperties.PREFIX)
public class HolidaysProperties {

	public static final String PREFIX = "ext.holidays";

	/**
	 * 自行扩展的 json 文件路径
	 */
	private List<ExtData> extData = new ArrayList<>();

	@Getter
	@Setter
	public static class ExtData {

		/**
		 * 年份
		 */
		private Integer year;

		/**
		 * 数据目录
		 */
		private String dataPath;

	}

}
