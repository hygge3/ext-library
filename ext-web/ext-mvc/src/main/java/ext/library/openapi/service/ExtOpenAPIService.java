package ext.library.openapi.service;

import ext.library.tool.util.ObjectUtil;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import jakarta.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义 OpenAPI 服务实现
 * <p>
 * 扩展 SpringDoc 的 {@link OpenAPIService}，增强 Tag 处理逻辑：
 * <ul>
 *   <li>支持使用 JavaDoc 注释作为 Tag 名称</li>
 *   <li>优化 Tag 的自动生成逻辑</li>
 * </ul>
 */
public class ExtOpenAPIService extends OpenAPIService {

    private final SecurityService securityParser;
    private final PropertyResolverUtils propertyResolverUtils;
    private final Optional<JavadocProvider> javadocProvider;

    /**
     * 构造自定义 OpenAPI 服务
     *
     * @param openAPI                   OpenAPI 实例
     * @param securityParser            安全解析器
     * @param springDocConfigProperties SpringDoc 配置属性
     * @param propertyResolverUtils     属性解析工具
     * @param openApiBuilderCustomizers OpenAPI 构建器自定义器
     * @param serverBaseUrlCustomizers  服务器基础 URL 自定义器
     * @param javadocProvider           JavaDoc 提供器
     */
    public ExtOpenAPIService(
            Optional<OpenAPI> openAPI,
            SecurityService securityParser,
            SpringDocConfigProperties springDocConfigProperties,
            PropertyResolverUtils propertyResolverUtils,
            Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers,
            Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers,
            Optional<JavadocProvider> javadocProvider) {
        super(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils,
                openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);

        this.securityParser = securityParser;
        this.propertyResolverUtils = propertyResolverUtils;
        this.javadocProvider = javadocProvider;
    }

    @Override
    public Operation buildTags(@Nonnull HandlerMethod handlerMethod, Operation operation,
                               OpenAPI openAPI, Locale locale) {
        Set<Tag> tags = new LinkedHashSet<>();
        Set<String> tagsStr = new LinkedHashSet<>();

        buildTagsFromMethod(handlerMethod.getMethod(), tags, tagsStr, locale);
        buildTagsFromClass(handlerMethod.getBeanType(), tags, tagsStr, locale);

        if (ObjectUtil.isNotEmpty(tagsStr)) {
            Set<String> resolvedTags = tagsStr.stream()
                    .map(str -> propertyResolverUtils.resolve(str, locale))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            mergeOperationTags(operation, resolvedTags);
        }

        if (isAutoTagClasses(operation)) {
            buildAutoTag(handlerMethod, operation, openAPI);
        }

        if (ObjectUtil.isNotEmpty(tags)) {
            // 配置或前序 Controller 已声明的同名 Tag 优先，避免同名两条
            Set<String> tagNames = tags.stream().map(Tag::getName).collect(Collectors.toSet());
            if (ObjectUtil.isNotEmpty(openAPI.getTags())) {
                openAPI.getTags().stream().filter(t -> tagNames.add(t.getName())).forEach(tags::add);
            }
            openAPI.setTags(new ArrayList<>(tags));
        }

        handleSecurityRequirements(handlerMethod, operation);
        return operation;
    }

    /**
     * 合并 Operation 的 Tag 集合
     */
    private void mergeOperationTags(Operation operation, Set<String> tagsToMerge) {
        if (ObjectUtil.isEmpty(operation.getTags())) {
            operation.setTags(new ArrayList<>(tagsToMerge));
        } else {
            Set<String> mergedTags = new LinkedHashSet<>(operation.getTags());
            mergedTags.addAll(tagsToMerge);
            operation.setTags(new ArrayList<>(mergedTags));
        }
    }

    /**
     * 处理操作级别的安全需求
     */
    private void handleSecurityRequirements(HandlerMethod handlerMethod, Operation operation) {
        io.swagger.v3.oas.annotations.security.SecurityRequirement[] securityRequirements =
                securityParser.getSecurityRequirements(handlerMethod);
        if (securityRequirements == null) {
            return;
        }
        if (securityRequirements.length == 0) {
            operation.setSecurity(Collections.emptyList());
        } else {
            securityParser.buildSecurityRequirement(securityRequirements, operation);
        }
    }

    /**
     * 自动构建 Tag：JavaDoc 第一行作 Tag 名，整段作描述；没有 JavaDoc 时退化为类名拆分。
     */
    private void buildAutoTag(HandlerMethod handlerMethod, Operation operation, OpenAPI openAPI) {
        Class<?> beanType = handlerMethod.getBeanType();
        String autoName = splitCamelCase(beanType.getSimpleName());
        String javadoc = javadocProvider.map(provider -> provider.getClassJavadoc(beanType)).orElse(null);
        String tagName = StringUtils.isBlank(javadoc)
                ? autoName
                : javadoc.lines().map(String::trim).filter(StringUtils::isNotBlank).findFirst().orElse(autoName);

        operation.addTagsItem(tagName);

        // Tag.equals 连 description 一起比，必须按 name 查重，否则同名 Tag 会出现两条
        boolean exists = openAPI.getTags() != null
                && openAPI.getTags().stream().anyMatch(tag -> tagName.equals(tag.getName()));
        if (exists) {
            return;
        }
        Tag tag = new Tag().name(tagName);
        if (StringUtils.isNotBlank(javadoc) && !tagName.equals(javadoc)) {
            tag.description(javadoc);
        }
        openAPI.addTagsItem(tag);
    }

    private void buildTagsFromMethod(Method method, Set<Tag> tags, Set<String> tagsStr, Locale locale) {
        Set<io.swagger.v3.oas.annotations.tags.Tag> methodTags =
                AnnotatedElementUtils.findAllMergedAnnotations(method, Tags.class).stream()
                        .flatMap(x -> Stream.of(x.value()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        methodTags.addAll(AnnotatedElementUtils.findAllMergedAnnotations(method,
                io.swagger.v3.oas.annotations.tags.Tag.class));

        if (ObjectUtil.isEmpty(methodTags)) {
            return;
        }
        tagsStr.addAll(methodTags.stream()
                .map(tag -> propertyResolverUtils.resolve(tag.name(), locale))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        addTags(new ArrayList<>(methodTags), tags, locale);
    }

    private void addTags(@Nonnull List<io.swagger.v3.oas.annotations.tags.Tag> sourceTags,
                         Set<Tag> tags, Locale locale) {
        Optional<Set<Tag>> optionalTagSet = AnnotationsUtils.getTags(
                sourceTags.toArray(new io.swagger.v3.oas.annotations.tags.Tag[0]), true);
        optionalTagSet.ifPresent(tagsSet -> tagsSet.forEach(tag -> {
            tag.name(propertyResolverUtils.resolve(tag.getName(), locale));
            tag.description(propertyResolverUtils.resolve(tag.getDescription(), locale));
            if (tags.stream().noneMatch(t -> t.getName().equals(tag.getName()))) {
                tags.add(tag);
            }
        }));
    }

}
