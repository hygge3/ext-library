package ext.library.openapi.service;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtOpenAPIServiceTest {

    private static final Locale LOCALE = Locale.SIMPLIFIED_CHINESE;

    private final OpenAPI openAPI = new OpenAPI();
    private final JavadocProvider javadocProvider = mock(JavadocProvider.class);
    private ExtOpenAPIService service;

    @BeforeEach
    void setUp() {
        SpringDocConfigProperties properties = new SpringDocConfigProperties();
        ConfigurableBeanFactory beanFactory = mock(ConfigurableBeanFactory.class);
        when(beanFactory.resolveEmbeddedValue(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        PropertyResolverUtils propertyResolverUtils = new PropertyResolverUtils(beanFactory, null, properties);
        service = new ExtOpenAPIService(Optional.of(openAPI), mock(SecurityService.class), properties,
                propertyResolverUtils, Optional.empty(), Optional.empty(), Optional.of(javadocProvider));
    }

    @Test
    void shouldUseFirstNonBlankJavadocLineAsTagName() throws Exception {
        when(javadocProvider.getClassJavadoc(DemoUserController.class))
                .thenReturn("\n\n用户管理接口\n\n更多说明\n");

        Operation operation = buildOperation("list");

        assertThat(operation.getTags()).containsExactly("用户管理接口");
        assertThat(openAPI.getTags()).singleElement().satisfies(tag -> {
            assertThat(tag.getName()).isEqualTo("用户管理接口");
            assertThat(tag.getDescription()).contains("更多说明");
        });
    }

    @Test
    void shouldFallbackToClassNameWhenJavadocMissing() throws Exception {
        when(javadocProvider.getClassJavadoc(DemoUserController.class)).thenReturn(null);

        assertThat(buildOperation("list").getTags()).containsExactly("demo-user-controller");
    }

    @Test
    void shouldNotDuplicateTagWithSameNameButDifferentDescription() throws Exception {
        when(javadocProvider.getClassJavadoc(DemoUserController.class)).thenReturn("用户管理接口\n说明");

        buildOperation("list");
        buildOperation("detail");

        assertThat(openAPI.getTags()).hasSize(1);
    }

    private Operation buildOperation(String methodName) throws Exception {
        HandlerMethod handlerMethod = new HandlerMethod(new DemoUserController(),
                DemoUserController.class.getDeclaredMethod(methodName));
        return service.buildTags(handlerMethod, new Operation(), openAPI, LOCALE);
    }

    @RestController
    static class DemoUserController {

        @GetMapping("/list")
        public List<String> list() {
            return List.of();
        }

        @GetMapping("/detail")
        public String detail() {
            return "";
        }
    }
}
