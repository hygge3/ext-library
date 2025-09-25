package ext.library.mybatis.config.properties;

import ext.library.mybatis.constant.DbField;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mybatis 自动配置属性
 */
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
    private String deleteField = DbField.DELETED;

    /** 开启内置的多租户实现，必须使用 TenantUtil 放入 */
    private Boolean tenant = false;

    public Boolean getSqlPrint() {
        return sqlPrint;
    }

    public void setSqlPrint(Boolean sqlPrint) {
        this.sqlPrint = sqlPrint;
    }

    public String getDeleteField() {
        return deleteField;
    }

    public void setDeleteField(String deleteField) {
        this.deleteField = deleteField;
    }

    public Boolean getTenant() {
        return tenant;
    }

    public void setTenant(Boolean tenant) {
        this.tenant = tenant;
    }
}