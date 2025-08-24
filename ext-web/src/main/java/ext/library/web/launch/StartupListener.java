package ext.library.web.launch;

import ext.library.tool.util.DateUtil;
import ext.library.tool.util.INetUtil;
import ext.library.tool.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

/**
 * 项目启动监听打印项目信息
 */
@AutoConfiguration
@RequiredArgsConstructor
public class StartupListener implements CommandLineRunner {

    private final Environment environment;

    private static boolean hasOpenApi() {
        return Stream.of("springfox.documentation.spring.web.plugins.Docket", "io.swagger.v3.oas.models.OpenAPI").anyMatch(clazz -> ClassUtils.isPresent(clazz, null));
    }

    @Override
    public void run(String... args) throws Exception {
        String appName = environment.getProperty("spring.application.name", "Application");
        String print = environment.getProperty("ext.web.print-startup-info", "true");
        if (ObjectUtil.isFalse(print)) {
            System.out.printf("🚀 %s 启动完成！", appName);
            return;
        }
        String serverPort = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        String ip = INetUtil.getHostIp();
        String hostName = INetUtil.getHostName();
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        LocalDateTime startTime = LocalDateTime.now();
        String startTimeFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sslEnable = environment.getProperty("server.ssl.enabled", "false");
        String scheme = ObjectUtil.isTrue(sslEnable) ? "https" : "http";
        String url = "%s://%s:%s%s".formatted(scheme, ip, serverPort, contextPath);
        String content = """
                %s
                        ✅ %s 启动完成！
                %s
                🎉 启动时间\t:\t%s
                📌 应用名称\t:\t%s
                🌐 访问地址\t:\t%s
                🏠 本机主机名\t:\t%s
                📍 本机 IP \t:\t%s
                💻 操作系统\t:\t%s (%s)
                %s
                """.formatted("=".repeat(60), appName, "=".repeat(60), DateUtil.format(startTime), appName, url, hostName, ip, osName, osArch, "=".repeat(60));
        boolean hasOpenApi = hasOpenApi();
        if (hasOpenApi) {
            // 如果有 swagger，打印开发阶段的 swagger ui 地址
            String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
            content = content.concat("""
                    📘 OpenAPI 文档\t:\t%s%s
                    """.formatted(url, swaggerPath));
        }
        System.out.println(content);
    }

}