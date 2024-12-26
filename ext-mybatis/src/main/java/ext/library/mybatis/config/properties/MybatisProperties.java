package ext.library.mybatis.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mybatis 自动配置属性
 */
@Data
@ConfigurationProperties(MybatisProperties.PREFIX)
public class MybatisProperties {

	static final String PREFIX = "ext.mybatis";

	/**
	 * 是否打开 SQL 执行日志
	 * <p>
	 * 默认：false
	 */
	private Boolean sqlPrint = false;

	/** 删除字段 */
	private String deleteField = "delete_time";

}
