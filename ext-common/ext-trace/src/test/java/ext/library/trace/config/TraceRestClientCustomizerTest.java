package ext.library.trace.config;

import ext.library.tool.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link TraceAutoConfiguration.RestClientTraceConfiguration} 的用例
 */
class TraceRestClientCustomizerTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void restClientSendsTraceHeader() {
        AtomicReference<MockClientHttpRequest> sent = new AtomicReference<>();
        RestClient.Builder builder = RestClient.builder().requestFactory((uri, method) -> {
            MockClientHttpRequest request = new MockClientHttpRequest(method, uri);
            request.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK));
            sent.set(request);
            return request;
        });

        new TraceAutoConfiguration.RestClientTraceConfiguration()
                .traceRestClientCustomizer()
                .customize(builder);

        builder.build().get().uri(URI.create("http://localhost/api")).retrieve().toBodilessEntity();

        assertNotNull(sent.get().getHeaders().getFirst(TraceContext.HEADER_REQUEST_ID), "RestClient 出站要带追踪头");
    }

}
