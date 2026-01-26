package ext.library.openapi.config;

import ext.library.openapi.properties.OpenApiProperties;
import ext.library.openapi.service.ExtOpenApiService;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * OpenAPI 自动配置类
 */
@EnableConfigurationProperties(OpenApiProperties.class)
@AutoConfigureBefore(SpringDocConfiguration.class)
@ConditionalOnProperty(prefix = OpenApiProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiAutoConfig {

    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    private final OpenApiProperties openApiProperties;
    private final ServerProperties serverProperties;

    public OpenApiAutoConfig(OpenApiProperties openApiProperties, ServerProperties serverProperties) {
        this.openApiProperties = openApiProperties;
        this.serverProperties = serverProperties;
    }

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI();

        // 文档基本信息
        OpenApiProperties.InfoProperties infoProperties = this.openApiProperties.getInfo();
        Info info = convertInfo(infoProperties);
        openAPI.info(info);

        // 扩展文档信息
        openAPI.externalDocs(this.openApiProperties.getExternalDocs());
        openAPI.servers(this.openApiProperties.getServers());
        openAPI.security(this.openApiProperties.getSecurity());
        openAPI.tags(this.openApiProperties.getTags());
        openAPI.paths(this.openApiProperties.getPaths());
        openAPI.components(this.openApiProperties.getComponents());
        openAPI.extensions(this.openApiProperties.getExtensions());

        return openAPI;
    }

    private Info convertInfo(OpenApiProperties.InfoProperties infoProperties) {
        Info info = new Info();
        info.setTitle(infoProperties.getTitle());
        info.setDescription(infoProperties.getDescription());
        info.setTermsOfService(infoProperties.getTermsOfService());
        info.setContact(infoProperties.getContact());
        info.setLicense(infoProperties.getLicense());
        info.setVersion(infoProperties.getVersion());
        info.setExtensions(infoProperties.getExtensions());
        return info;
    }

    /**
     * 自定义 OpenAPI 服务
     */
    @Bean
    public OpenAPIService openApiBuilder(
            Optional<OpenAPI> openAPI,
            SecurityService securityParser,
            SpringDocConfigProperties springDocConfigProperties,
            PropertyResolverUtils propertyResolverUtils,
            Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers,
            Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers,
            Optional<JavadocProvider> javadocProvider) {
        Logs.info(EmojiSymbol.OPENAPI, "OpenAPI module loaded");
        return new ExtOpenApiService(openAPI, securityParser, springDocConfigProperties,
                propertyResolverUtils, openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);
    }

    /**
     * 对已生成的 OpenAPI 进行自定义处理
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        String contextPath = serverProperties.getServlet().getContextPath();
        String finalContextPath = ObjectUtil.isEmpty(contextPath) || "/".equals(contextPath)
                ? ""
                : contextPath;

        return openApi -> {
            // 对所有路径增加前置上下文路径
            Paths oldPaths = openApi.getPaths();
            if (oldPaths instanceof PrefixedPaths) {
                return;
            }
            PrefixedPaths newPaths = new PrefixedPaths();
            oldPaths.forEach((k, v) -> newPaths.addPathItem(finalContextPath + k, v));
            openApi.setPaths(newPaths);

            // 将 Controller 的 JavaDoc 注释转换为 Tag 名称
            if (openApi.getTags() == null) {
                return;
            }
            convertJavadocToTagName(openApi);
        };
    }

    /**
     * 将 JavaDoc 描述转换为 Tag 名称
     */
    private void convertJavadocToTagName(OpenAPI openApi) {
        for (Tag tag : openApi.getTags()) {
            if (!ALPHA_NUMERIC_PATTERN.matcher(tag.getName()).matches()
                    || StringUtil.isEmpty(tag.getDescription())) {
                continue;
            }
            String oldName = tag.getName();
            String newName = tag.getDescription();

            // 同步修改所有 Operation 中的 Tag 引用
            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperations().forEach(operation ->
                            replaceTagName(operation, oldName, newName)));
            tag.name(newName);
        }
    }

    /**
     * 替换 Operation 中的 Tag 名称
     */
    private void replaceTagName(Operation operation, String oldName, String newName) {
        if (operation.getTags() == null) {
            return;
        }
        operation.getTags().replaceAll(tagName -> tagName.equals(oldName) ? newName : tagName);
    }

    /**
     * 标记已处理路径的 Paths 子类，用于避免重复添加 contextPath 前缀
     */
    static class PrefixedPaths extends Paths {
    }

}
