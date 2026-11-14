package ext.library.trace.support;

import ext.library.tool.trace.TraceContext;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 出站请求的追踪头注入
 * <p>
 * 自动挂到容器里的每个 {@code RestTemplate} 上（见 {@code TraceAutoConfiguration}）。
 * 显式设置过的同名头不覆盖。
 *
 * @since 4.1.0
 */
public class TraceInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                                 @NonNull byte[] body,
                                                 @NonNull ClientHttpRequestExecution execution) throws IOException {
        for (Map.Entry<String, String> header : TraceContext.outboundHeaders(
                request.getHeaders().getFirst(TraceContext.HEADER_REQUEST_ID)).entrySet()) {
            if (request.getHeaders().getFirst(header.getKey()) == null) {
                request.getHeaders().set(header.getKey(), header.getValue());
            }
        }
        return execution.execute(request, body);
    }

}
