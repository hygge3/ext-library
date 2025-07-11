package ext.library.web.launch;

import ext.library.tool.$;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ClassUtils;

import jakarta.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * 项目启动事件通知
 */
@AutoConfiguration
public class StartedEventListener {

    private static boolean hasOpenApi() {
        return Stream.of("springfox.documentation.spring.web.plugins.Docket", "io.swagger.v3.oas.models.OpenAPI").anyMatch(clazz -> ClassUtils.isPresent(clazz, null));
    }

    @Async
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(@Nonnull WebServerInitializedEvent event) {
        WebServerApplicationContext context = event.getApplicationContext();
        Environment environment = context.getEnvironment();
        String appName = $.defaultIfEmpty(environment.getProperty("spring.application.name"), "APP");
        String env = $.defaultIfEmpty(environment.getProperty("spring.profiles.active"), "default");
        String contextPath = $.defaultIfEmpty(environment.getProperty("server.servlet.context-path"), "/");
        int localPort = event.getWebServer().getPort();
        ApplicationHome home = new ApplicationHome();
        String content = """
                === [%s] Startup, port:[%d],contextPath:[%s] env:[%s] ===
                dir: %s, source: %s
                swagger: %b, path: %s
                """;
        boolean hasOpenApi = hasOpenApi();
        // 如果有 swagger，打印开发阶段的 swagger ui 地址
        String swaggerPath = $.defaultIfEmpty(environment.getProperty("springdoc.swagger-ui.path"), hasOpenApi ? "/swagger-ui.html" : null);
        System.err.printf(content, appName, localPort, contextPath, env, home.getDir(), home.getSource(), hasOpenApi(), swaggerPath);
    }

}