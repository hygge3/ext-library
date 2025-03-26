package ext.library.web.config;

import jakarta.annotation.Nonnull;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

/**
 * 为 WebSocketDeploymentInfo 设置合理的参数
 */
@AutoConfiguration
public class UndertowAutoConfiguration implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Override
    public void customize(@Nonnull UndertowServletWebServerFactory factory) {
        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            // 设置合理的参数
            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(true, 8192));
            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
        });
    }
}
