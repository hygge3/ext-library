package ext.library.web.launch;

import java.util.stream.Stream;

import ext.library.tool.$;
import org.jetbrains.annotations.NotNull;
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

/**
 * 项目启动事件通知
 */
@AutoConfiguration
public class StartedEventListener {

    @Async
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(@NotNull WebServerInitializedEvent event) {
        WebServerApplicationContext context = event.getApplicationContext();
        Environment environment = context.getEnvironment();
        String appName = environment.getProperty("spring.application.name");
        String profile = environment.getProperty("spring.profiles.active");
        int localPort = event.getWebServer().getPort();
        System.err.printf("=== [%s] Startup, port:[%d], env:[%s] ===%n", $.defaultIfEmpty(appName, "APP"), localPort,
                $.defaultIfEmpty(profile, "default"));

        ApplicationHome home = new ApplicationHome();
        System.out.printf("dir: %s, source: %s%n", home.getDir(), home.getSource());

        // 如果有 swagger，打印开发阶段的 swagger ui 地址
        if (hasOpenApi()) {
            String property = environment.getProperty("springdoc.swagger-ui.path");
            String swaggerPath = $.defaultIfEmpty(property, "/swagger-ui.html");
            System.out.printf("http://localhost:%s%s%n", localPort, swaggerPath);
        } else {
            System.out.printf("http://localhost:%s%n", localPort);
        }
    }

    private static boolean hasOpenApi() {
        return Stream.of("springfox.documentation.spring.web.plugins.Docket", "io.swagger.v3.oas.models.OpenAPI")
                .anyMatch(clazz -> ClassUtils.isPresent(clazz, null));
    }

}
