package ext.library.http;

import java.io.IOException;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import ext.library.tool.holder.retry.IRetry;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/**
 * 重试拦截器，应对代理问题
 */
@RequiredArgsConstructor
public class RetryInterceptor implements Interceptor {

    private final IRetry retry;

    @Nullable
    private final Predicate<ResponseSpec> respPredicate;

    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return retry.execute(() -> {
            Response response = chain.proceed(request);
            // 结果集校验

            if (respPredicate == null) {
                return response;
            }
            // copy 一份 body
            ResponseBody body = response.peekBody(Long.MAX_VALUE);
            try (HttpResponse httpResponse = new HttpResponse(response)) {
                if (respPredicate.test(httpResponse)) {
                    throw new IOException("Http 重试失败。");
                }
            }
            return response.newBuilder().body(body).build();
        });
    }

}
