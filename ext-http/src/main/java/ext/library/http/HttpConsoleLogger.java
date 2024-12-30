package ext.library.http;

import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * OkHttp console log.
 */
public enum HttpConsoleLogger implements HttpLoggingInterceptor.Logger {

    /**
     * 实例
     */
    INSTANCE;

    public void log(@NotNull @Nls String message) {
        // 统一添加前缀，方便在茫茫日志中查看
        System.out.println("[🔗] " + message);
    }

}
