package ext.library.config.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * openAPI 属性
 */
@Data
@ConfigurationProperties(OpenApiProperties.PREFIX)
public class OpenApiProperties {

    public static final String PREFIX = "ext.openapi";

    /**
     * 是否开启 openApi 文档
     */
    Boolean enabled = true;

    /**
     * 文档基本信息
     */
    @NestedConfigurationProperty
    InfoProperties info = new InfoProperties();

    /**
     * 扩展文档地址
     */
    @NestedConfigurationProperty
    ExternalDocumentation externalDocs;

    /**
     * Api 服务
     *
     * @see <a href="https://swagger.io/docs/specification/api-host-and-base-path/">API
     * Server and Base URL</a>
     */
    List<Server> servers = null;

    /**
     * 安全配置
     *
     * @see <a href="https://swagger.io/docs/specification/authentication/">Authentication
     */
    List<SecurityRequirement> security = null;

    /**
     * 标签
     */
    List<Tag> tags = null;

    /**
     * 路径
     */
    @NestedConfigurationProperty
    Paths paths = null;

    /**
     * 组件
     */
    @NestedConfigurationProperty
    Components components = null;

    /**
     * 扩展信息
     * <p>
     * map 类型属性没有 IDE
     * 提示，<a href="https://github.com/spring-projects/spring-boot/issues/9945">gh-9945</a>
     */
    Map<String, Object> extensions = null;

    /**
     * <p>
     * 文档的基础属性信息
     * </p>
     *
     * @see io.swagger.v3.oas.models.info.Info
     * <p>
     * 为了 springboot 自动生产配置提示信息，所以这里复制一个类出来
     */
    @Data
    public static class InfoProperties {

        /**
         * 标题
         */
        String title = null;

        /**
         * 描述
         */
        String description = null;

        /**
         * 服务条款 URL
         */
        String termsOfService = null;

        /**
         * 联系人信息
         */
        @NestedConfigurationProperty
        Contact contact = null;

        /**
         * 许可证
         */
        @NestedConfigurationProperty
        License license = null;

        /**
         * 版本
         */
        String version = null;

        /**
         * 扩展属性
         */
        Map<String, Object> extensions = null;

    }

}
