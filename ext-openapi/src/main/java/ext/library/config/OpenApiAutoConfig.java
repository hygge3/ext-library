package ext.library.config;

import ext.library.config.properties.OpenApiProperties;
import ext.library.handler.OpenApiHandler;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * OpenAPI 的自动配置类
 */
@EnableConfigurationProperties(OpenApiProperties.class)
@AutoConfigureBefore(SpringDocConfiguration.class)
@ConditionalOnProperty(prefix = OpenApiProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final OpenApiProperties openApiProperties;

    private final ServerProperties serverProperties;

    public OpenApiAutoConfig(OpenApiProperties openApiProperties, ServerProperties serverProperties) {
        this.openApiProperties = openApiProperties;
        this.serverProperties = serverProperties;
    }

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
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
     * 自定义 openapi 处理器
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openAPI, SecurityService securityParser, SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils, Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers, Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers, Optional<JavadocProvider> javadocProvider) {
        log.info("[📃] OpenAPI 模块载入成功");
        return new OpenApiHandler(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils, openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);
    }

    /**
     * 对已经生成好的 OpenApi 进行自定义操作
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        String contextPath = serverProperties.getServlet().getContextPath();
        String finalContextPath;
        if (ObjectUtil.isEmpty(contextPath) || Symbol.SLASH.equals(contextPath)) {
            finalContextPath = Symbol.EMPTY;
        } else {
            finalContextPath = contextPath;
        }
        return openApi -> {
            // 对所有路径增加前置上下文路径
            Paths oldPaths = openApi.getPaths();
            if (oldPaths instanceof PlusPaths) {
                return;
            }
            PlusPaths newPaths = new PlusPaths();
            oldPaths.forEach((k, v) -> newPaths.addPathItem(finalContextPath + k, v));
            openApi.setPaths(newPaths);


            // 把 controller 上面的注释 生成 TAG
            // 官方默认是把注释弄成了 description，但是我们希望他的变成 tag
            if (openApi.getTags() == null) {
                return;
            }
            List<Tag> tags = openApi.getTags();
            for (Tag tag : tags) {
                if (Pattern.compile("^[a-zA-Z0-9-]+$").asPredicate().test(tag.getName()) && StringUtil.isNotEmpty(tag.getDescription())) {
                    String newName = tag.getDescription();

                    // 同步修改映射关系
                    openApi.getPaths().forEach((s, pathItem) -> {
                        List<Operation> operations = new ArrayList<>();
                        addIfNotNull(operations, pathItem.getGet());
                        addIfNotNull(operations, pathItem.getDelete());
                        addIfNotNull(operations, pathItem.getDelete());
                        addIfNotNull(operations, pathItem.getOptions());
                        addIfNotNull(operations, pathItem.getPatch());
                        addIfNotNull(operations, pathItem.getPost());
                        addIfNotNull(operations, pathItem.getPut());
                        addIfNotNull(operations, pathItem.getTrace());

                        for (Operation operation : operations) {
                            for (int i = 0; i < operation.getTags().size(); i++) {
                                if (operation.getTags().get(i).equals(tag.getName())) {
                                    operation.getTags().set(i, newName);
                                }
                            }
                        }

                    });
                    tag.name(newName);
                }
            }
        };
    }

    private <T> void addIfNotNull(List<T> original, T itemsToAdd) {
        if (itemsToAdd != null) {
            original.add(itemsToAdd);
        }
    }

    /**
     * 单独使用一个类便于判断 解决 springdoc 路径拼接重复问题
     *
     * @author Lion Li
     */
    static class PlusPaths extends Paths {

        public PlusPaths() {
            super();
        }

    }

}