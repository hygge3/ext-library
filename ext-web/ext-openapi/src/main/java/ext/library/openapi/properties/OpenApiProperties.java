package ext.library.openapi.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;

/**
 * OpenAPI 配置属性
 */
@ConfigurationProperties(OpenApiProperties.PREFIX)
public class OpenApiProperties {

    public static final String PREFIX = "ext.openapi";

    /**
     * 是否开启 OpenAPI 文档
     */
    private Boolean enabled = true;

    /**
     * 文档基本信息
     */
    @NestedConfigurationProperty
    private InfoProperties info = new InfoProperties();

    /**
     * 扩展文档地址
     */
    @NestedConfigurationProperty
    private ExternalDocumentation externalDocs;

    /**
     * API 服务器配置
     *
     * @see <a href="https://swagger.io/docs/specification/api-host-and-base-path/">API Server and Base URL</a>
     */
    private List<Server> servers;

    /**
     * 安全配置
     *
     * @see <a href="https://swagger.io/docs/specification/authentication/">Authentication</a>
     */
    private List<SecurityRequirement> security;

    /**
     * 标签列表
     */
    private List<Tag> tags;

    /**
     * 路径配置
     */
    @NestedConfigurationProperty
    private Paths paths;

    /**
     * 组件配置
     */
    @NestedConfigurationProperty
    private Components components;

    /**
     * 扩展信息
     * <p>
     * Map 类型属性没有 IDE 提示，
     * 参考 <a href="https://github.com/spring-projects/spring-boot/issues/9945">gh-9945</a>
     */
    private Map<String, Object> extensions;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public InfoProperties getInfo() {
        return info;
    }

    public void setInfo(InfoProperties info) {
        this.info = info;
    }

    public ExternalDocumentation getExternalDocs() {
        return externalDocs;
    }

    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public List<SecurityRequirement> getSecurity() {
        return security;
    }

    public void setSecurity(List<SecurityRequirement> security) {
        this.security = security;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Paths getPaths() {
        return paths;
    }

    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    /**
     * 文档基础属性信息
     *
     * @see io.swagger.v3.oas.models.info.Info
     */
    public static class InfoProperties {

        /**
         * 标题
         */
        private String title;

        /**
         * 描述
         */
        private String description;

        /**
         * 服务条款 URL
         */
        private String termsOfService;

        /**
         * 联系人信息
         */
        @NestedConfigurationProperty
        private Contact contact;

        /**
         * 许可证
         */
        @NestedConfigurationProperty
        private License license;

        /**
         * 版本
         */
        private String version;

        /**
         * 扩展属性
         */
        private Map<String, Object> extensions;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTermsOfService() {
            return termsOfService;
        }

        public void setTermsOfService(String termsOfService) {
            this.termsOfService = termsOfService;
        }

        public Contact getContact() {
            return contact;
        }

        public void setContact(Contact contact) {
            this.contact = contact;
        }

        public License getLicense() {
            return license;
        }

        public void setLicense(License license) {
            this.license = license;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Map<String, Object> getExtensions() {
            return extensions;
        }

        public void setExtensions(Map<String, Object> extensions) {
            this.extensions = extensions;
        }
    }

}
