package ext.library.mybatis.config.properties;

import ext.library.mybatis.constant.DbField;
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
    Boolean sqlPrint = false;

    /** 删除字段 */
    String deleteField = DbField.DELETED;

    /** 开启内置的多租户实现 */
    Boolean tenant = false;

}
